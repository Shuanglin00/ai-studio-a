package com.shuanglin.train.model.framework;

import com.shuanglin.train.config.ModelProperties;
import com.shuanglin.train.model.base.DjlModel;

/**
 * GGUF 框架模型抽象基类
 * 提供 GGUF (llama.cpp) 格式模型的通用实现
 */
public abstract class AbstractGgufModel implements DjlModel {

    protected final ModelProperties.ModelConfig config;
    protected boolean loaded = false;

    protected AbstractGgufModel(ModelProperties.ModelConfig config) {
        this.config = config;
    }

    @Override
    public String getModelId() {
        return config.getName() != null ? config.getName() : config.getModelPath();
    }

    @Override
    public String getVersion() {
        return config.getVersion() != null ? config.getVersion() : "latest";
    }

    @Override
    public boolean isLoaded() {
        return loaded;
    }

    /**
     * 获取设备配置
     */
    protected String getDevice() {
        return config.getDevice();
    }

    /**
     * 获取线程数
     */
    protected int getThreads() {
        return config.getThreads();
    }

    /**
     * 获取批量大小
     */
    protected int getBatchSize() {
        return config.getBatchSize();
    }
}
