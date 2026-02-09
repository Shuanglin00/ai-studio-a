package com.shuanglin.train.model.type;

/**
 * 模型类型枚举
 * 定义支持的模型功能类型
 */
public enum ModelType {
    /**
     * 文本生成模型
     */
    TEXT_MODEL("textModel", "文本生成模型"),

    /**
     * 文本嵌入模型
     */
    EMBEDDING_MODEL("embeddingModel", "文本嵌入模型"),

    /**
     * 图像生成模型
     */
    IMAGE_MODEL("imageModel", "图像生成模型"),

    /**
     * 图像理解模型
     */
    IMAGE_UNDERSTANDING_MODEL("imageUnderstandingModel", "图像理解模型"),

    /**
     * 语音识别模型
     */
    SPEECH_RECOGNITION_MODEL("speechRecognitionModel", "语音识别模型"),

    /**
     * 语音合成模型
     */
    SPEECH_SYNTHESIS_MODEL("speechSynthesisModel", "语音合成模型"),

    /**
     * 重排序模型
     */
    RERANK_MODEL("rerankModel", "重排序模型");

    private final String code;
    private final String description;

    ModelType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据 code 获取枚举
     */
    public static ModelType fromCode(String code) {
        for (ModelType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的模型类型: " + code);
    }
}
