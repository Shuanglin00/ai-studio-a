package com.shuanglin.train.model.impl;

import com.shuanglin.train.config.ModelProperties;
import com.shuanglin.train.model.framework.AbstractHuggingfaceModel;
import com.shuanglin.train.model.type.ModelType;
import com.shuanglin.train.model.type.RerankModel;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * HuggingFace 重排序模型实现
 * 架构层级: model:bge-rerank-huggingface -> type:rerankModel -> framework:huggingface -> impl:HuggingfaceRerankModel
 */
@Slf4j
public class HuggingfaceRerankModel extends AbstractHuggingfaceModel implements RerankModel {

    public HuggingfaceRerankModel(ModelProperties.ModelConfig config) {
        super(config);
        if (config.getType() != ModelType.RERANK_MODEL) {
            throw new IllegalArgumentException("模型类型不匹配，期望 RERANK_MODEL");
        }
    }

    @Override
    public ModelType getModelType() {
        return ModelType.RERANK_MODEL;
    }

    @Override
    public void load() {
        log.info("加载 HuggingFace 重排序模型: {}", getModelId());
        // TODO: 使用 DJL 加载 HuggingFace cross-encoder 模型
        this.loaded = true;
    }

    @Override
    public void unload() {
        log.info("卸载 HuggingFace 重排序模型: {}", getModelId());
        // TODO: 释放模型资源
        this.loaded = false;
    }

    @Override
    public List<Integer> rerank(String query, List<String> documents) {
        ensureLoaded();
        log.debug("重排序，查询: {}, 文档数: {}", query, documents.size());
        // TODO: 调用重排序模型获取相关性分数
        // 这里需要实现实际的 rerank 逻辑，目前返回按原始顺序排列的索引
        // 实际实现应该返回按相关性从高到低排序的文档索引
        List<Integer> indices = new java.util.ArrayList<>();
        for (int i = 0; i < documents.size(); i++) {
            indices.add(i);
        }
        return indices;
    }

    @Override
    public List<RerankResult> rerankWithScores(String query, List<String> documents) {
        ensureLoaded();
        log.debug("带分数重排序，查询: {}, 文档数: {}", query, documents.size());
        // TODO: 带分数重排序 - 需要实现实际的评分逻辑
        // 目前返回默认分数，实际应返回基于模型推理的相关性分数
        List<RerankResult> results = new java.util.ArrayList<>();
        for (int i = 0; i < documents.size(); i++) {
            results.add(new RerankResult(i, 0.0)); // 默认分数，需要实际计算
        }
        return results;
    }

    private void ensureLoaded() {
        if (!loaded) {
            load();
        }
    }
}
