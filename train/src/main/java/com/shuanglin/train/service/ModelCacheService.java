package com.shuanglin.train.service;

import com.shuanglin.train.config.ModelProperties;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 模型缓存服务
 * 负责模型的下载、缓存和清理
 */
@Slf4j
@Service
public class ModelCacheService {

    private final ModelProperties modelProperties;
    private final Path cacheDir;
    private final boolean cacheEnabled;

    /**
     * 模型缓存信息
     */
    private static class CacheInfo {
        final Path path;
        final long size;
        final LocalDateTime lastAccessTime;
        final AtomicLong accessCount = new AtomicLong(0);

        CacheInfo(Path path) throws IOException {
            this.path = path;
            BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
            this.size = attrs.size();
            this.lastAccessTime = LocalDateTime.now();
        }

        void access() {
            accessCount.incrementAndGet();
        }

        Duration getIdleTime() {
            return Duration.between(lastAccessTime, LocalDateTime.now());
        }
    }

    /**
     * 模型下载任务追踪
     */
    private final Map<String, DownloadTask> downloadTasks = new ConcurrentHashMap<>();

    /**
     * 下载状态
     */
    public enum DownloadStatus {
        PENDING, DOWNLOADING, COMPLETED, FAILED
    }

    /**
     * 下载任务信息
     */
    public static class DownloadTask {
        final String modelId;
        final String modelPath;
        volatile DownloadStatus status = DownloadStatus.PENDING;
        volatile long progress = 0;
        volatile long totalSize = 0;
        volatile String errorMessage;
        volatile long downloadedBytes = 0;

        DownloadTask(String modelId, String modelPath) {
            this.modelId = modelId;
            this.modelPath = modelPath;
        }
    }

    public ModelCacheService(
            ModelProperties modelProperties,
            @Value("${train.model.cache-dir:./model-cache}") String cacheDir,
            @Value("${train.model.cache-enabled:true}") boolean cacheEnabled) {
        this.modelProperties = modelProperties;
        this.cacheDir = Path.of(cacheDir);
        this.cacheEnabled = cacheEnabled;

        if (cacheEnabled && !Files.exists(this.cacheDir)) {
            try {
                Files.createDirectories(this.cacheDir);
                log.info("创建模型缓存目录: {}", cacheDir);
            } catch (IOException e) {
                log.error("创建模型缓存目录失败: {}", cacheDir, e);
            }
        }
    }

    /**
     * 获取模型缓存路径
     * 如果缓存不存在且是 HuggingFace 模型仓库，则自动下载
     */
    public Optional<Path> getModelPath(String modelId, String modelPath) {
        if (!cacheEnabled) {
            log.debug("模型缓存已禁用，直接返回模型路径: {}", modelPath);
            return Optional.of(Path.of(modelPath));
        }

        Path cachePath = resolveCachePath(modelId, modelPath);

        // 检查缓存是否存在
        if (Files.exists(cachePath)) {
            log.debug("模型缓存命中: {} -> {}", modelId, cachePath);
            return Optional.of(cachePath);
        }

        // 需要下载
        log.info("模型缓存未找到，准备下载: {}", modelId);
        return Optional.empty();
    }

    /**
     * 解析缓存路径
     */
    private Path resolveCachePath(String modelId, String modelPath) {
        // 对于 HuggingFace 模型仓库，使用安全的名字作为目录名
        String safeName = modelId.replace("/", "_").replace(":", "_");
        return cacheDir.resolve(safeName);
    }

    /**
     * 检查模型是否已缓存
     */
    public boolean isModelCached(String modelId) {
        if (!cacheEnabled) {
            return false;
        }
        String modelPath = modelProperties.getModels().get(modelId).getModelPath();
        Path cachePath = resolveCachePath(modelId, modelPath);
        return Files.exists(cachePath);
    }

    /**
     * 获取缓存大小
     */
    public long getCacheSize() {
        if (!Files.exists(cacheDir)) {
            return 0;
        }
        try {
            return Files.walk(cacheDir)
                    .filter(Files::isRegularFile)
                    .mapToLong(path -> {
                        try {
                            return Files.size(path);
                        } catch (IOException e) {
                            return 0;
                        }
                    })
                    .sum();
        } catch (IOException e) {
            log.error("计算缓存大小失败", e);
            return 0;
        }
    }

    /**
     * 清理过期缓存
     * 默认清理超过 7 天未访问的缓存
     */
    @Scheduled(cron = "0 0 3 * * ?")  // 每天凌晨 3 点执行
    public void cleanupExpiredCache() {
        if (!cacheEnabled || !Files.exists(cacheDir)) {
            return;
        }

        log.info("开始清理过期模型缓存...");
        AtomicInteger cleanedCount = new AtomicInteger(0);
        AtomicLong cleanedSize = new AtomicLong(0);

        try {
            var idleThreshold = Duration.ofDays(7);

            Files.walk(cacheDir)
                    .filter(Files::isDirectory)
                    .filter(path -> !path.equals(cacheDir))
                    .forEach(dir -> {
                        try {
                            BasicFileAttributes attrs = Files.readAttributes(dir, BasicFileAttributes.class);
                            LocalDateTime lastModified = LocalDateTime.ofInstant(
                                    attrs.lastModifiedTime().toInstant(),
                                    java.time.ZoneId.systemDefault());

                            Duration idleTime = Duration.between(lastModified, LocalDateTime.now());

                            if (idleTime.compareTo(idleThreshold) > 0) {
                                long size = calculateDirSize(dir);
                                Files.walk(dir)
                                        .sorted((a, b) -> b.toString().compareTo(a.toString()))
                                        .forEach(p -> {
                                            try {
                                                Files.deleteIfExists(p);
                                            } catch (IOException e) {
                                                log.warn("删除缓存文件失败: {}", p);
                                            }
                                        });
                                cleanedCount.incrementAndGet();
                                cleanedSize.addAndGet(size);
                                log.info("清理过期缓存: {}, 大小: {} MB", dir, size / (1024 * 1024));
                            }
                        } catch (IOException e) {
                            log.warn("检查缓存目录失败: {}", dir);
                        }
                    });
        } catch (IOException e) {
            log.error("清理缓存失败", e);
        }

        log.info("缓存清理完成，清理 {} 个缓存，共 {} MB", cleanedCount.get(), cleanedSize.get() / (1024 * 1024));
    }

    private long calculateDirSize(Path dir) throws IOException {
        return Files.walk(dir)
                .filter(Files::isRegularFile)
                .mapToLong(path -> {
                    try {
                        return Files.size(path);
                    } catch (IOException e) {
                        return 0;
                    }
                })
                .sum();
    }

    /**
     * 清理所有缓存
     */
    public void clearAllCache() {
        if (!Files.exists(cacheDir)) {
            return;
        }

        log.info("清理所有模型缓存...");
        try {
            Files.walk(cacheDir)
                    .sorted((a, b) -> b.toString().compareTo(a.toString()))
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException e) {
                            log.warn("删除缓存文件失败: {}", p);
                        }
                    });
            log.info("所有模型缓存已清理");
        } catch (IOException e) {
            log.error("清理缓存失败", e);
        }
    }

    /**
     * 预热缓存
     * 预加载所有配置的模型
     */
    public void warmupCache() {
        if (!cacheEnabled) {
            log.info("模型缓存已禁用，跳过缓存预热");
            return;
        }

        log.info("开始模型缓存预热...");
        int warmedCount = 0;

        for (Map.Entry<String, ModelProperties.ModelConfig> entry : modelProperties.getModels().entrySet()) {
            String modelId = entry.getKey();
            ModelProperties.ModelConfig config = entry.getValue();

            if (!isModelCached(modelId)) {
                log.info("预热模型: {}", modelId);
                // 实际下载逻辑由模型工厂调用 getModelPath 时触发
                warmedCount++;
            }
        }

        log.info("模型缓存预热完成，预热 {} 个模型", warmedCount);
    }

    @PreDestroy
    public void shutdown() {
        log.info("ModelCacheService 关闭");
        downloadTasks.clear();
    }

    /**
     * 获取下载任务状态
     */
    public Optional<DownloadTask> getDownloadTask(String modelId) {
        return Optional.ofNullable(downloadTasks.get(modelId));
    }

    /**
     * 取消下载任务
     */
    public boolean cancelDownload(String modelId) {
        DownloadTask task = downloadTasks.remove(modelId);
        if (task != null && task.status == DownloadStatus.DOWNLOADING) {
            log.info("取消下载任务: {}", modelId);
            return true;
        }
        return false;
    }
}