package com.shuanglin.framework.onebot.sender;

import com.shuanglin.framework.onebot.config.RetryProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

/**
 * 重试策略工具类
 */
@Slf4j
@RequiredArgsConstructor
public class RetryPolicy {

    private final RetryProperties retryProperties;

    /**
     * 执行带重试的操作
     *
     * @param operation 要执行的操作
     * @param <T>       返回类型
     * @return 操作结果，失败则返回 null
     */
    public <T> T execute(Supplier<T> operation) {
        int maxAttempts = retryProperties.getMaxAttempts();
        long delayMs = retryProperties.getDelayMs();
        double multiplier = retryProperties.getMultiplier();

        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.get();
            } catch (Exception e) {
                lastException = e;
                log.warn("Attempt {}/{} failed: {}", attempt, maxAttempts, e.getMessage());

                if (attempt < maxAttempts) {
                    try {
                        Thread.sleep(delayMs);
                        delayMs = (long) (delayMs * multiplier);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("Retry interrupted", ie);
                        break;
                    }
                }
            }
        }

        log.error("All {} attempts failed", maxAttempts, lastException);
        return null;
    }

    /**
     * 执行带重试的操作（无返回值）
     *
     * @param operation 要执行的操作
     */
    public void execute(Runnable operation) {
        execute(() -> {
            operation.run();
            return null;
        });
    }
}
