package com.shuanglin.train.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.UUID;

/**
 * 模型调用 AOP 切面
 * 记录模型方法调用的来源、时间和耗时
 */
@Slf4j
@Aspect
@Component
public class ModelCallAspect {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * 环绕通知：拦截所有 DjlModel 实现类的方法调用
     */
    @Around("execution(* com.shuanglin.train.model.base.DjlModel+.*(..))")
    public Object logModelCall(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取调用来源信息
        String callerName = getCallerName();
        String callTime = LocalDateTime.now().format(FORMATTER);
        String callId = UUID.randomUUID().toString().substring(0, 8);
        String modelInfo = getModelInfo(joinPoint);
        String methodName = joinPoint.getSignature().getName();
        String args = formatArgs(joinPoint.getArgs());

        log.info("[ModelCall] [{}] {} - {} 调用 {}.{} 参数: {}",
                callId, callTime, callerName, modelInfo, methodName, args);

        long startTime = System.nanoTime();
        try {
            Object result = joinPoint.proceed();
            long durationNs = System.nanoTime() - startTime;
            double durationMs = durationNs / 1_000_000.0;

            String resultInfo = formatResult(result);
            log.info("[ModelCall] [{}] 完成调用 {}.{} 耗时: {}ms",
                     callId, modelInfo, methodName, String.format("%.2f", durationMs));

            return result;
        } catch (Exception e) {
            long durationNs = System.nanoTime() - startTime;
            double durationMs = durationNs / 1_000_000.0;
            log.error("[ModelCall] [{}] 调用 {}.{} 耗时: {}ms 异常: {}",
                    callId, modelInfo, methodName, String.format("%.2f", durationMs), e.getMessage());
            throw e;
        }
    }

    /**
     * 获取调用方名称
     */
    private String getCallerName() {
        // 优先从 HTTP 请求中获取
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                String uri = request.getRequestURI();
                String method = request.getMethod();
                // 尝试从请求头获取调用方标识
                String caller = request.getHeader("X-Caller-Name");
                if (caller != null && !caller.isEmpty()) {
                    return caller;
                }
                return String.format("%s %s", method, uri);
            }
        } catch (Exception ignored) {
        }

        // 从调用栈中获取
        StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        for (StackTraceElement element : stackTrace) {
            String className = element.getClassName();
            // 跳过 AOP 和框架相关的类
            if (!className.startsWith("com.shuanglin.train.aspect") &&
                    !className.startsWith("org.springframework") &&
                    !className.startsWith("jdk.internal")) {
                // 提取简化的类名
                String simpleClassName = className.substring(className.lastIndexOf('.') + 1);
                return String.format("%s.%s()", simpleClassName, element.getMethodName());
            }
        }

        return "Unknown";
    }

    /**
     * 获取模型 ID
     */
    private String getModelInfo(ProceedingJoinPoint joinPoint) {
        Object target = joinPoint.getTarget();
        if (target instanceof com.shuanglin.train.model.base.DjlModel) {
            return ((com.shuanglin.train.model.base.DjlModel) target).getModelInfo();
        }
        return target.getClass().getSimpleName();
    }

    /**
     * 格式化参数
     */
    private String formatArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }
        return Arrays.toString(args);
    }

    /**
     * 格式化结果
     */
    private String formatResult(Object result) {
        if (result == null) {
            return "null";
        }
        if (result instanceof String) {
            String str = (String) result;
            if (str.length() > 100) {
                return "\"" + str.substring(0, 100) + "...\"";
            }
            return "\"" + str + "\"";
        }
        if (result instanceof float[]) {
            float[] arr = (float[]) result;
            return String.format("float[%d] (dim=%d)", arr.length, arr.length);
        }
        if (result instanceof double[]) {
            double[] arr = (double[]) result;
            return String.format("double[%d]", arr.length);
        }
        if (result instanceof int[]) {
            int[] arr = (int[]) result;
            return String.format("int[%d] %s", arr.length, Arrays.toString(arr));
        }
        if (result instanceof java.util.List) {
            java.util.List<?> list = (java.util.List<?>) result;
            return String.format("List[%d]", list.size());
        }
        return result.getClass().getSimpleName();
    }
}
