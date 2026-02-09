package com.shuanglin.train.service;

import com.shuanglin.train.config.ModelExecutorConfig;
import com.shuanglin.train.exception.ModelException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * 模型异步执行服务
 * 提供线程池管理、请求队列、结果回调和失败重试
 */
@Slf4j
@Service
public class ModelAsyncService {

    private final ModelExecutorConfig executorConfig;
    private final ModelRetryService retryService;

    /**
     * 等待队列 - 存储待执行的模型任务
     */
    private final BlockingQueue<ModelTask<?>> taskQueue = new LinkedBlockingQueue<>(1000);

    /**
     * 正在执行的任务数量
     */
    private final AtomicInteger activeTaskCount = new AtomicInteger(0);

    /**
     * 任务执行统计
     */
    private final Map<String, AtomicLong> taskStats = new ConcurrentHashMap<>();

    @Autowired
    public ModelAsyncService(
            ModelExecutorConfig executorConfig,
            ModelRetryService retryService) {
        this.executorConfig = executorConfig;
        this.retryService = retryService;

        // 启动队列消费者
        startQueueConsumer();
    }

    /**
     * 异步执行模型调用
     */
    public <T> CompletableFuture<T> executeAsync(
            String modelId,
            Function<String, T> task,
            int retryCount) {

        CompletableFuture<T> future = new CompletableFuture<>();

        ModelTask<T> modelTask = new ModelTask<>(
                modelId,
                task,
                retryCount,
                future,
                null
        );

        // 尝试直接执行或放入队列
        if (!enqueueTask(modelTask)) {
            future.completeExceptionally(
                    new ModelException(modelId, "任务队列已满，请稍后重试",
                            ModelException.ErrorType.RESOURCE_EXHAUSTED)
            );
        }

        return future;
    }

    /**
     * 异步执行模型调用，带回调
     */
    public <T> void executeAsync(
            String modelId,
            Function<String, T> task,
            int retryCount,
            BiConsumer<T, Throwable> callback) {

        executeAsync(modelId, task, retryCount)
                .whenComplete((result, throwable) -> {
                    if (callback != null) {
                        callback.accept(result, throwable);
                    }
                });
    }

    /**
     * 批量异步执行模型调用
     */
    public <T> List<CompletableFuture<T>> executeBatch(
            String modelId,
            List<Function<String, T>> tasks,
            int retryCount) {

        return tasks.stream()
                .map(task -> executeAsync(modelId, task, retryCount))
                .toList();
    }

    /**
     * 同步执行模型调用（带重试）
     */
    public <T> T executeSync(String modelId, Function<String, T> task, int retryCount) {
        Executor executor = executorConfig.getDedicatedExecutor(modelId);

        try {
            return retryService.executeWithRetry(
                    () -> {
                        incrementStat(modelId, "submitted");
                        return task.apply(modelId);
                    },
                    retryCount,
                    modelId
            );
        } catch (Exception e) {
            incrementStat(modelId, "failed");
            throw e;
        }
    }

    /**
     * 尝试将任务加入队列或直接执行
     */
    private <T> boolean enqueueTask(ModelTask<T> task) {
        int active = activeTaskCount.get();
        int processors = Runtime.getRuntime().availableProcessors();

        // 如果活跃任务数小于核心线程数，直接执行
        if (active < processors * 2) {
            executeTask(task);
            return true;
        }

        // 否则尝试加入队列
        return taskQueue.offer(task);
    }

    /**
     * 执行任务
     */
    private <T> void executeTask(ModelTask<T> task) {
        activeTaskCount.incrementAndGet();
        incrementStat(task.modelId, "started");

        Executor executor = executorConfig.getDedicatedExecutor(task.modelId);

        CompletableFuture.runAsync(() -> {
            try {
                T result = task.execute();
                task.future.complete(result);
                incrementStat(task.modelId, "completed");
            } catch (Exception e) {
                incrementStat(task.modelId, "failed");

                if (task.retryCount > 0) {
                    // 重试任务
                    retryTask(task, e);
                } else {
                    task.future.completeExceptionally(e);
                }
            } finally {
                activeTaskCount.decrementAndGet();
                // 尝试从队列中取下一个任务
                pollAndExecute();
            }
        }, executor);
    }

    /**
     * 重试任务
     */
    private <T> void retryTask(ModelTask<T> task, Throwable e) {
        retryService.scheduleRetry(() -> {
            log.debug("重试任务 {}，剩余重试次数: {}", task.modelId, task.retryCount - 1);
            executeTask(task.withDecrementedRetry());
        }, task.retryCount);
    }

    /**
     * 从队列中取出并执行任务
     */
    private void pollAndExecute() {
        ModelTask<?> task = taskQueue.poll();
        if (task != null) {
            executeTask(task);
        }
    }

    /**
     * 启动队列消费者线程
     */
    private void startQueueConsumer() {
        Thread consumer = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    ModelTask<?> task = taskQueue.poll(1, TimeUnit.SECONDS);
                    if (task != null) {
                        executeTask(task);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "model-task-consumer");
        consumer.setDaemon(true);
        consumer.start();
    }

    /**
     * 获取队列状态
     */
    public QueueStatus getQueueStatus() {
        return new QueueStatus(
                taskQueue.size(),
                activeTaskCount.get(),
                taskQueue.remainingCapacity(),
                getStats()
        );
    }

    /**
     * 获取任务统计
     */
    public Map<String, AtomicLong> getStats() {
        return new ConcurrentHashMap<>(taskStats);
    }

    private void incrementStat(String modelId, String key) {
        taskStats.computeIfAbsent(modelId + "." + key, k -> new AtomicLong(0))
                .incrementAndGet();
    }

    /**
     * 模型任务
     */
    private static class ModelTask<T> {
        final String modelId;
        final Function<String, T> task;
        int retryCount;
        final CompletableFuture<T> future;

        ModelTask(String modelId, Function<String, T> task, int retryCount,
                  CompletableFuture<T> future, T result) {
            this.modelId = modelId;
            this.task = task;
            this.retryCount = retryCount;
            this.future = future;
        }

        T execute() {
            return task.apply(modelId);
        }

        ModelTask<T> withDecrementedRetry() {
            this.retryCount--;
            return this;
        }
    }

    /**
     * 队列状态
     */
    public record QueueStatus(
            int queueSize,
            int activeCount,
            int remainingCapacity,
            Map<String, AtomicLong> stats
    ) {}
}
