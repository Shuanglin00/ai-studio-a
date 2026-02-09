package com.shuanglin.train.model.type;

import com.shuanglin.train.model.base.DjlModel;

import java.util.List;

/**
 * 文本嵌入模型接口
 * 用于文本向量化/RAG 场景
 */
public interface EmbeddingModel extends DjlModel {

    /**
     * 生成单条文本的嵌入向量
     * @param text 输入文本
     * @return 嵌入向量
     */
    float[] embed(String text);

    /**
     * 批量生成文本嵌入向量
     * @param texts 输入文本列表
     * @return 嵌入向量列表
     */
    List<float[]> embedBatch(List<String> texts);

    /**
     * 获取嵌入向量的维度
     * @return 向量维度
     */
    int getDimension();

    /**
     * 获取模型名称
     */
    String getModelName();
}
