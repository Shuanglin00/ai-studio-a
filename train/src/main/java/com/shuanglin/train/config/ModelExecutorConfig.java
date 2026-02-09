package com.shuanglin.train.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 模型执行线程池配置
 * 支持自适应线程池和按模型类型分离线程池
 */
@Slf4j
@Configuration
@EnableAsync
public class ModelExecutorConfig {

    /**
     * 线程池名称常量
     */
    public static final String MODEL_EXECUTOR = "modelExecutor";
    public static final String MODEL_IO_EXECUTOR = "modelIoExecutor";
    public static final String MODEL_CPU_EXECUTOR = "modelCpuExecutor";
    public static final String MODEL_DEDICATED_EXECUTOR_TEMPLATE = "modelDedicated-{}";

    /**
     * 管理的所有线程池
     */
    private final List<ThreadPoolExecutor> managedExecutors = new CopyOnWriteArrayList<>();

    /**
     * 模型专用线程池缓存
     */
    private final Map<String, ThreadPoolTaskExecutor> dedicatedExecutors = new ConcurrentHashMap<>();

    /**
     * 默认模型执行线程池 (CPU密集型)
     * 用于模型推理等计算密集任务
     */
    @Bean(MODEL_CPU_EXECUTOR)
    public Executor modelCpuExecutor() {
        int processors = Runtime.getRuntime().availableProcessors();
        int poolSize = Math.max(2, processors);

        ThreadPoolTaskExecutor executor = createThreadPool(
                poolSize,
                poolSize * 2,
                100,
                "model-cpu-"
        );

        log.info("初始化CPU密集型模型线程池: core={}, max={}, queue={}",
                poolSize, poolSize * 2, 100);
        return executor;
    }

    /**
     * IO密集型模型线程池
     * 用于网络请求类模型调用
     */
    @Bean(MODEL_IO_EXECUTOR)
    public Executor modelIoExecutor() {
        int processors = Runtime.getRuntime().availableProcessors();
        int poolSize = processors * 2;

        ThreadPoolTaskExecutor executor = createThreadPool(
                poolSize,
                poolSize * 2,
                200,
                "model-io-"
        );

        log.info("初始化IO密集型模型线程池: core={}, max={}, queue={}",
                poolSize, poolSize * 2, 200);
        return executor;
    }

    /**
     * 默认模型执行线程池
     * 用于通用模型调用任务
     */
    @Bean(MODEL_EXECUTOR)
    public Executor modelExecutor() {
        int processors = Runtime.getRuntime().availableProcessors();

        ThreadPoolTaskExecutor executor = createThreadPool(
                processors,
                processors * 2,
                100,
                "model-exec-"
        );

        log.info("初始化默认模型线程池: core={}, max={}, queue={}",
                processors, processors * 2, 100);
        return executor;
    }

    /**
     * 创建线程池
     */
    protected ThreadPoolTaskExecutor createThreadPool(
            int corePoolSize,
            int maxPoolSize,
            int queueCapacity,
            String threadNamePrefix) {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();


        managedExecutors.add(executor.getThreadPoolExecutor());
        return executor;
    }

    /**
     * 获取模型的专用线程池
     */
    public ThreadPoolTaskExecutor getDedicatedExecutor(String modelId) {
        return dedicatedExecutors.computeIfAbsent(modelId, this::createDedicatedExecutor);
    }

    /**
     * 为指定模型创建专用线程池
     */
    private ThreadPoolTaskExecutor createDedicatedExecutor(String modelId) {
        log.debug("为模型 {} 创建专用线程池", modelId);

        String prefix = "model-" + modelId.substring(0, Math.min(8, modelId.length())) + "-";
        ThreadPoolTaskExecutor executor = createThreadPool(2, 4, 50, prefix);

        log.info("模型 {} 专用线程池已创建", modelId);
        return executor;
    }

    /**
     * 移除模型的专用线程池
     */
    public void removeDedicatedExecutor(String modelId) {
        ThreadPoolTaskExecutor executor = dedicatedExecutors.remove(modelId);
        if (executor != null) {
            executor.shutdown();
            log.info("模型 {} 专用线程池已移除", modelId);
        }
    }

    /**
     * 获取所有管理的线程池
     */
    public List<ThreadPoolExecutor> getManagedExecutors() {
        return List.copyOf(managedExecutors);
    }

    /**
     * 关闭所有线程池
     */
    public void shutdownAll() {
        log.info("关闭所有模型线程池，共 {} 个", managedExecutors.size());
        managedExecutors.forEach(executor -> {
            try {
                executor.shutdown();
            } catch (Exception e) {
                log.warn("关闭线程池失败", e);
            }
        });
        managedExecutors.clear();

        dedicatedExecutors.values().forEach(executor -> {
            try {
                executor.shutdown();
            } catch (Exception e) {
                log.warn("关闭专用线程池失败", e);
            }
        });
        dedicatedExecutors.clear();
    }
}
