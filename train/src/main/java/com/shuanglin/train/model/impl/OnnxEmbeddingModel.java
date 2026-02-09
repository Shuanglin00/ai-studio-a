package com.shuanglin.train.model.impl;

import com.shuanglin.train.config.ModelProperties;
import com.shuanglin.train.model.framework.AbstractOnnxModel;
import com.shuanglin.train.model.type.EmbeddingModel;
import com.shuanglin.train.model.type.ModelType;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * ONNX 嵌入模型实现
 * 架构层级: model:bge-onnx -> type:embeddingModel -> framework:onnx -> impl:OnnxEmbeddingModel
 */
@Slf4j
public class OnnxEmbeddingModel extends AbstractOnnxModel implements EmbeddingModel {

    private int dimension = 0;

    public OnnxEmbeddingModel(ModelProperties.ModelConfig config) {
        super(config);
        if (config.getType() != ModelType.EMBEDDING_MODEL) {
            throw new IllegalArgumentException("模型类型不匹配，期望 EMBEDDING_MODEL");
        }
    }

    @Override
    public ModelType getModelType() {
        return ModelType.EMBEDDING_MODEL;
    }

    @Override
    public void load() {
        log.info("加载 ONNX 嵌入模型: {}, 路径: {}", getModelId(), config.getModelPath());
        // TODO: 使用 DJL ONNX Runtime 加载嵌入模型
        this.loaded = true;
        this.dimension = 1024; // 示例维度
    }

    @Override
    public void unload() {
        log.info("卸载 ONNX 嵌入模型: {}", getModelId());
        // TODO: 释放模型资源
        this.loaded = false;
    }

    @Override
    public float[] embed(String text) {
        ensureLoaded();
        log.debug("生成嵌入向量，文本长度: {}", text.length());
        // TODO: 调用 ONNX Runtime 生成嵌入
        return new float[dimension];
    }

    @Override
    public List<float[]> embedBatch(List<String> texts) {
        ensureLoaded();
        log.debug("批量生成嵌入，数量: {}", texts.size());
        // TODO: 批量生成
        return texts.stream().map(this::embed).toList();
    }

    @Override
    public int getDimension() {
        return dimension;
    }

    @Override
    public String getModelName() {
        return getModelId();
    }

    private void ensureLoaded() {
        if (!loaded) {
            load();
        }
    }
}
