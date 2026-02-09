package com.shuanglin.train.model.type;

import com.shuanglin.train.model.base.DjlModel;

import java.util.List;

/**
 * 重排序模型接口
 * 用于 RAG 场景中的结果重排序
 */
public interface RerankModel extends DjlModel {

    /**
     * 对文档进行重排序
     * @param query 查询文本
     * @param documents 文档列表
     * @return 重排序后的文档索引（按相关性从高到低）
     */
    List<Integer> rerank(String query, List<String> documents);

    /**
     * 对文档进行重排序并返回分数
     * @param query 查询文本
     * @param documents 文档列表
     * @return 包含索引和分数的对象列表
     */
    List<RerankResult> rerankWithScores(String query, List<String> documents);

    /**
     * 重排序结果
     */
    record RerankResult(int index, double score) {}

    /**
     * 获取返回的最大文档数
     */
    default int getMaxDocs() {
        return 10;
    }
}
