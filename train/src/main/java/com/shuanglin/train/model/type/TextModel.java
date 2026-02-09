package com.shuanglin.train.model.type;

import com.shuanglin.train.model.base.DjlModel;

import java.util.List;

/**
 * 文本生成模型接口
 * 用于文本生成/聊天场景
 */
public interface TextModel extends DjlModel {

    /**
     * 文本生成（同步）
     * @param prompt 提示词
     * @return 生成的文本
     */
    String generate(String prompt);

    /**
     * 带参数的文本生成
     * @param prompt 提示词
     * @param maxTokens 最大生成 token 数
     * @param temperature 温度参数
     * @param topP top-p 采样参数
     * @return 生成的文本
     */
    String generate(String prompt, int maxTokens, float temperature, float topP);

    /**
     * 批量文本生成
     * @param prompts 提示词列表
     * @return 生成的文本列表
     */
    List<String> generateBatch(List<String> prompts);

    /**
     * 流式文本生成
     * @param prompt 提示词
     * @return 文本块流
     */
    default TextStreamResult streamGenerate(String prompt) {
        throw new UnsupportedOperationException("流式生成暂不支持");
    }

    /**
     * 流式生成结果
     */
    interface TextStreamResult extends Iterable<String> {
        /**
         * 获取完整响应（流结束后）
         */
        String getFullResponse();

        /**
         * 是否流已结束
         */
        boolean isDone();
    }
}
