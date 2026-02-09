package com.shuanglin.train.model.framework;

import com.shuanglin.train.config.ModelProperties;
import com.shuanglin.train.model.base.DjlModel;

/**
 * ONNX 框架模型抽象基类
 * 提供 ONNX 格式模型的通用实现
 */
public abstract class AbstractOnnxModel implements DjlModel {

    protected final ModelProperties.ModelConfig config;
    protected boolean loaded = false;

    protected AbstractOnnxModel(ModelProperties.ModelConfig config) {
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
}
