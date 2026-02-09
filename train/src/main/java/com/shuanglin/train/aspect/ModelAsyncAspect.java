package com.shuanglin.train.aspect;

import com.shuanglin.train.model.base.DjlModel;
import com.shuanglin.train.service.ModelAsyncService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * 模型异步调用切面
 * 将模型方法调用转换为异步执行，支持队列和重试
 */
@Slf4j
@Aspect
@Component
public class ModelAsyncAspect {

    private final ModelAsyncService asyncService;

    public ModelAsyncAspect(ModelAsyncService asyncService) {
        this.asyncService = asyncService;
    }

    /**
     * 环绕通知：拦截所有 DjlModel 实现类的返回 CompletableFuture 的方法
     */
    @Around("execution(* com.shuanglin.train.model.base.DjlModel+.*(..)) && " +
            "@annotation(org.springframework.scheduling.annotation.Async)")
    public Object handleAsyncCall(ProceedingJoinPoint joinPoint) throws Throwable {
        // 对于被 @Async 标记的方法，由 Spring 异步执行
        // 这里不做额外处理，只是记录调用信息
        return joinPoint.proceed();
    }

    /**
     * 环绕通知：拦截带有 @ModelAsync 注解的方法
     */
    @Around("@annotation(modelAsync)")
    public Object handleModelAsync(
            ProceedingJoinPoint joinPoint,
            ModelAsync modelAsync) throws Throwable {

        String callId = UUID.randomUUID().toString().substring(0, 8);
        String modelInfo = getModelInfo(joinPoint);
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        // 判断是否强制异步执行
        if (modelAsync.forceAsync()) {
            return executeAsync(joinPoint, callId, modelInfo, methodName, args);
        }

        // 判断是否应该异步执行
        int concurrencyLevel = modelAsync.concurrencyLevel();
        var queueStatus = asyncService.getQueueStatus();

        if (queueStatus.queueSize() > concurrencyLevel ||
                queueStatus.activeCount() > Runtime.getRuntime().availableProcessors() * 2) {
            log.debug("[ModelAsync] [{}] 队列繁忙，切换到异步模式", callId);
            return executeAsync(joinPoint, callId, modelInfo, methodName, args);
        }

        // 同步执行
        return joinPoint.proceed();
    }

    /**
     * 异步执行方法
     */
    private Object executeAsync(
            ProceedingJoinPoint joinPoint,
            String callId,
            String modelInfo,
            String methodName,
            Object[] args) throws Throwable {

        log.info("[ModelAsync] [{}] 异步执行 {}.{}", callId, modelInfo, methodName);

        Object target = joinPoint.getTarget();
        String modelId = getModelId(target);

        // 构建任务
        CompletableFuture<Object> future = asyncService.executeAsync(
                modelId,
                mid -> {
                    try {
                        // 使用反射调用方法
                        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
                        return signature.getMethod().invoke(target, args);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                },
                3 // 默认重试次数
        );

        // 如果调用方在等待结果，使用 get() 阻塞获取
        if (Boolean.TRUE.equals(joinPoint.getArgs()[0])) {
            try {
                Object result = future.get();
                log.info("[ModelAsync] [{}] 异步执行完成", callId);
                return result;
            } catch (ExecutionException e) {
                if (e.getCause() instanceof RuntimeException re) {
                    throw re.getCause();
                }
                throw e;
            }
        }

        // 否则立即返回 CompletableFuture
        return future;
    }

    /**
     * 获取模型 ID
     */
    private String getModelId(Object target) {
        if (target instanceof DjlModel model) {
            return model.getModelId();
        }
        return target.getClass().getSimpleName();
    }

    /**
     * 获取模型信息
     */
    private String getModelInfo(ProceedingJoinPoint joinPoint) {
        Object target = joinPoint.getTarget();
        if (target instanceof DjlModel model) {
            return model.getModelInfo();
        }
        return target.getClass().getSimpleName();
    }

    /**
     * 模型异步执行注解
     */
    @interface ModelAsync {
        /**
         * 强制异步执行
         */
        boolean forceAsync() default false;

        /**
         * 队列繁忙时的并发阈值
         */
        int concurrencyLevel() default 50;

        /**
         * 重试次数
         */
        int retryCount() default 3;
    }
}
