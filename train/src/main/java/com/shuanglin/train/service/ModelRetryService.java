package com.shuanglin.train.service;

import com.shuanglin.train.exception.ModelException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * 模型重试服务
 * 提供指数退避重试和失败快速失败机制
 */
@Slf4j
@Service
public class ModelRetryService {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    /**
     * 默认最大重试次数
     */
    @Value("${train.model.retry.max-attempts:3}")
    private int maxAttempts;

    /**
     * 默认初始延迟(ms)
     */
    @Value("${train.model.retry.initial-delay-ms:100}")
    private long initialDelayMs;

    /**
     * 默认最大延迟(ms)
     */
    @Value("${train.model.retry.max-delay-ms:5000}")
    private long maxDelayMs;

    /**
     * 重试延迟乘数
     */
    private static final double DELAY_MULTIPLIER = 2.0;

    /**
     * 重试任务追踪
     */
    private final ConcurrentHashMap<String, RetryTracker> retryTrackers = new ConcurrentHashMap<>();

    /**
     * 执行带重试的操作
     */
    public <T> T executeWithRetry(Supplier<T> supplier, int maxRetries, String modelId) {
        int attempts = 0;
        long delay = initialDelayMs;

        while (true) {
            try {
                return supplier.get();
            } catch (Exception e) {
                attempts++;

                // 判断是否可重试
                if (!isRetryable(e) || attempts > maxRetries) {
                    log.error("[Retry] {} 达到最大重试次数 {}，放弃重试",
                            modelId, attempts - 1);
                    throw e;
                }

                // 记录重试
                recordRetryAttempt(modelId, attempts, e);

                log.warn("[Retry] {} 第 {} 次失败，{}ms 后重试: {}",
                        modelId, attempts, delay, e.getMessage());

                // 指数退避
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new ModelException(modelId, "重试被中断", ModelException.ErrorType.UNKNOWN, ie);
                }

                delay = (long) Math.min(delay * DELAY_MULTIPLIER, maxDelayMs);
            }
        }
    }

    /**
     * 异步执行带重试的操作
     */
    public <T> CompletableFuture<T> executeWithRetryAsync(
            Supplier<T> supplier,
            int maxRetries,
            String modelId) {

        return CompletableFuture.supplyAsync(() ->
                executeWithRetry(supplier, maxRetries, modelId)
        );
    }

    /**
     * 调度重试任务
     */
    public void scheduleRetry(Runnable task, int retryCount) {
        if (retryCount <= 0) {
            task.run();
            return;
        }

        long delay = (long) Math.min(initialDelayMs * Math.pow(DELAY_MULTIPLIER, 3 - retryCount), maxDelayMs);

        scheduler.schedule(() -> {
            try {
                task.run();
            } catch (Exception e) {
                log.error("重试任务执行失败", e);
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    /**
     * 判断异常是否可重试
     */
    private boolean isRetryable(Exception e) {
        // ModelException 判断
        if (e instanceof ModelException modelEx) {
            return modelEx.isRetryable();
        }

        // 网络相关异常可重试
        if (e instanceof java.net.SocketTimeoutException ||
                e instanceof java.net.ConnectException ||
                e instanceof java.io.IOException) {
            return true;
        }

        // 超时异常
        if (e.getCause() instanceof TimeoutException) {
            return true;
        }

        // 检查异常消息
        String message = e.getMessage();
        if (message != null) {
            return message.contains("timeout") ||
                    message.contains("connection refused") ||
                    message.contains("temporary failure") ||
                    message.contains("service unavailable");
        }

        return false;
    }

    /**
     * 记录重试尝试
     */
    private void recordRetryAttempt(String modelId, int attempt, Exception e) {
        RetryTracker tracker = retryTrackers.computeIfAbsent(
                modelId + ":" + System.currentTimeMillis() / 60000,
                k -> new RetryTracker()
        );
        tracker.record(attempt, e);
    }

    /**
     * 获取重试统计
     */
    public Map<String, RetryTracker> getRetryStats() {
        return new ConcurrentHashMap<>(retryTrackers);
    }

    /**
     * 清空统计
     */
    public void clearStats() {
        retryTrackers.clear();
    }

    /**
     * 重试追踪器
     */
    public static class RetryTracker {
        private final AtomicInteger totalAttempts = new AtomicInteger(0);
        private final AtomicInteger successCount = new AtomicInteger(0);
        private final AtomicInteger failCount = new AtomicInteger(0);
        private volatile long lastRetryTime;

        void record(int attempt, Exception e) {
            totalAttempts.addAndGet(attempt);
            lastRetryTime = System.currentTimeMillis();
            if (e == null) {
                successCount.incrementAndGet();
            } else {
                failCount.incrementAndGet();
            }
        }

        public int getTotalAttempts() {
            return totalAttempts.get();
        }

        public int getSuccessCount() {
            return successCount.get();
        }

        public int getFailCount() {
            return failCount.get();
        }

        public long getLastRetryTime() {
            return lastRetryTime;
        }
    }
}
