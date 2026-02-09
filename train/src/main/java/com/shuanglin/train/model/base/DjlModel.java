package com.shuanglin.train.model.base;

import com.shuanglin.train.model.type.ModelType;

/**
 * DJL 模型基础接口
 * 所有具体模型实现都应实现此接口
 */
public interface DjlModel {

    /**
     * 获取模型标识符
     */
    String getModelId();

    /**
     * 获取模型类型
     */
    ModelType getModelType();

    /**
     * 获取模型版本
     */
    String getVersion();

    /**
     * 检查模型是否已加载
     */
    boolean isLoaded();

    /**
     * 加载模型
     */
    void load();

    /**
     * 卸载模型，释放资源
     */
    void unload();

    /**
     * 获取模型信息描述
     */
    default String getModelInfo() {
        return String.format("Model[id=%s, type=%s, version=%s, loaded=%s]",
                getModelId(), getModelType(), getVersion(), isLoaded());
    }
}
