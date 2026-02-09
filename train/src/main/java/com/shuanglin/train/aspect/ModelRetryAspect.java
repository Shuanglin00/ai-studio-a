package com.shuanglin.train.aspect;

import com.shuanglin.train.exception.ModelException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 模型调用重试和熔断切面
 *
 * 使用示例:
 * <pre>
 * {@code
 * @ModelOperation(retryCount = 3, circuitBreaker = true)
 * public String generate(String prompt) { ... }
 * }
 * </pre>
 */
@Slf4j
@Aspect
@Component
public class ModelRetryAspect {

    /**
     * 熔断器状态
     */
    private enum CircuitState {
        CLOSED, OPEN, HALF_OPEN
    }

    /**
     * 熔断器信息
     */
    private static class CircuitBreakerInfo {
        volatile CircuitState state = CircuitState.CLOSED;
        volatile LocalDateTime lastFailureTime;
        volatile int failureCount = 0;
        final AtomicInteger successCount = new AtomicInteger(0);

        private static final int FAILURE_THRESHOLD = 5;
        private static final int SUCCESS_THRESHOLD = 3;
        private static final Duration OPEN_TIMEOUT = Duration.ofMinutes(1);
    }

    private final ConcurrentHashMap<String, CircuitBreakerInfo> circuitBreakers = new ConcurrentHashMap<>();

    /**
     * 模型操作注解
     */
    @interface ModelOperation {
        int retryCount() default 1;
        boolean circuitBreaker() default false;
        long timeoutMs() default 30000;
    }

    /**
     * 环绕通知，处理重试和熔断
     */
    @Around("@annotation(modelOperation)")
    public Object handleRetryAndCircuitBreaker(
            ProceedingJoinPoint joinPoint,
            ModelOperation modelOperation) throws Throwable {

        String methodName = getMethodName(joinPoint);
        String modelId = extractModelId(joinPoint);
        String cacheKey = modelId + ":" + methodName;

        // 熔断器检查
        if (modelOperation.circuitBreaker()) {
            CircuitBreakerInfo breaker = getCircuitBreaker(cacheKey);
            if (!allowRequest(breaker)) {
                log.warn("[CircuitBreaker] 请求被拒绝，熔断器已打开: {}", cacheKey);
                throw new ModelException(modelId, "服务暂时不可用，请稍后重试",
                        ModelException.ErrorType.RESOURCE_EXHAUSTED);
            }
        }

        // 重试逻辑
        int maxRetries = modelOperation.retryCount();
        Throwable lastException = null;

        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                long startTime = System.currentTimeMillis();
                Object result = joinPoint.proceed();
                long duration = System.currentTimeMillis() - startTime;

                log.debug("[ModelCall] {} 成功，耗时: {}ms", methodName, duration);

                // 重置熔断器
                if (modelOperation.circuitBreaker()) {
                    recordSuccess(cacheKey);
                }

                return result;
            } catch (Exception e) {
                lastException = e;
                ModelException.ErrorType errorType = classifyError(e);

                log.warn("[ModelCall] {} 第 {} 次尝试失败: {}",
                        methodName, attempt + 1, e.getMessage());

                // 判断是否应该重试
                if (shouldRetry(e, attempt, maxRetries)) {
                    Thread.sleep(calculateBackoff(attempt));
                    continue;
                }

                // 记录熔断器失败
                if (modelOperation.circuitBreaker()) {
                    recordFailure(cacheKey);
                }

                throw transformException(modelId, e, errorType);
            }
        }

        // 所有重试都失败
        throw transformException(modelId, lastException, ModelException.ErrorType.UNKNOWN);
    }

    private String getMethodName(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getDeclaringType().getSimpleName() + "." + signature.getName();
    }

    private String extractModelId(ProceedingJoinPoint joinPoint) {
        Object target = joinPoint.getTarget();
        if (target != null) {
            try {
                Method getModelId = target.getClass().getMethod("getModelId");
                Object modelId = getModelId.invoke(target);
                if (modelId != null) {
                    return modelId.toString();
                }
            } catch (Exception ignored) {
            }
        }
        return "unknown";
    }

    private CircuitBreakerInfo getCircuitBreaker(String key) {
        return circuitBreakers.computeIfAbsent(key, k -> new CircuitBreakerInfo());
    }

    private boolean allowRequest(CircuitBreakerInfo breaker) {
        if (breaker.state == CircuitState.CLOSED) {
            return true;
        }
        if (breaker.state == CircuitState.OPEN) {
            // 检查是否超时
            if (breaker.lastFailureTime != null &&
                    Duration.between(breaker.lastFailureTime, LocalDateTime.now())
                            .compareTo(CircuitBreakerInfo.OPEN_TIMEOUT) > 0) {
                // 转换为半开状态
                breaker.state = CircuitState.HALF_OPEN;
                breaker.successCount.set(0);
                log.info("[CircuitBreaker] 转换到半开状态");
                return true;
            }
            return false;
        }
        // HALF_OPEN 状态，允许有限数量的请求
        return true;
    }

    private void recordSuccess(String key) {
        CircuitBreakerInfo breaker = circuitBreakers.get(key);
        if (breaker != null) {
            if (breaker.state == CircuitState.HALF_OPEN) {
                int count = breaker.successCount.incrementAndGet();
                if (count >= CircuitBreakerInfo.SUCCESS_THRESHOLD) {
                    breaker.state = CircuitState.CLOSED;
                    breaker.failureCount = 0;
                    log.info("[CircuitBreaker] 熔断器关闭恢复正常");
                }
            } else {
                breaker.failureCount = 0;
            }
        }
    }

    private void recordFailure(String key) {
        CircuitBreakerInfo breaker = circuitBreakers.get(key);
        if (breaker != null) {
            breaker.failureCount++;
            breaker.lastFailureTime = LocalDateTime.now();

            if (breaker.failureCount >= CircuitBreakerInfo.FAILURE_THRESHOLD) {
                breaker.state = CircuitState.OPEN;
                log.warn("[CircuitBreaker] 熔断器打开，连续失败: {}",
                        breaker.failureCount);
            }
        }
    }

    private boolean shouldRetry(Exception e, int attempt, int maxRetries) {
        if (attempt >= maxRetries) {
            return false;
        }

        // 可重试的异常类型
        if (e instanceof java.net.SocketTimeoutException ||
                e instanceof java.net.ConnectException ||
                e instanceof java.io.IOException ||
                e.getCause() instanceof java.util.concurrent.TimeoutException) {
            return true;
        }

        // 检查异常消息
        String message = e.getMessage();
        if (message != null && (
                message.contains("timeout") ||
                        message.contains("connection refused") ||
                        message.contains("temporary failure"))) {
            return true;
        }

        return false;
    }

    private ModelException.ErrorType classifyError(Exception e) {
        String message = e.getMessage();
        if (message == null) {
            return ModelException.ErrorType.UNKNOWN;
        }

        if (message.contains("timeout")) {
            return ModelException.ErrorType.TIMEOUT;
        }
        if (message.contains("not found") || message.contains("不存在")) {
            return ModelException.ErrorType.MODEL_NOT_FOUND;
        }
        if (message.contains("out of memory") || message.contains("资源耗尽")) {
            return ModelException.ErrorType.RESOURCE_EXHAUSTED;
        }
        if (message.contains("load") || message.contains("加载")) {
            return ModelException.ErrorType.LOAD_FAILED;
        }
        if (message.contains("predict") || message.contains("预测")) {
            return ModelException.ErrorType.PREDICTION_FAILED;
        }

        return ModelException.ErrorType.UNKNOWN;
    }

    private RuntimeException transformException(String modelId, Throwable e,
                                                ModelException.ErrorType errorType) {
        if (e instanceof ModelException) {
            return (ModelException) e;
        }
        return new ModelException(modelId, e.getMessage(), errorType, e);
    }

    private long calculateBackoff(int attempt) {
        // 指数退避: 100ms, 200ms, 400ms, ...
        return Math.min((long) (100 * Math.pow(2, attempt)), 5000);
    }

    /**
     * 获取熔断器状态（用于监控）
     */
    public CircuitState getCircuitState(String modelId, String methodName) {
        String key = modelId + ":" + methodName;
        CircuitBreakerInfo breaker = circuitBreakers.get(key);
        return breaker != null ? breaker.state : CircuitState.CLOSED;
    }

    /**
     * 重置熔断器
     */
    public void resetCircuitBreaker(String modelId, String methodName) {
        String key = modelId + ":" + methodName;
        circuitBreakers.remove(key);
        log.info("[CircuitBreaker] 熔断器已重置: {}", key);
    }

    /**
     * 重置所有熔断器
     */
    public void resetAllCircuitBreakers() {
        circuitBreakers.clear();
        log.info("[CircuitBreaker] 所有熔断器已重置");
    }
}