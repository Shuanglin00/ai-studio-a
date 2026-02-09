package com.shuanglin.train.config;

import com.shuanglin.train.model.base.DjlModel;
import com.shuanglin.train.model.type.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 模型提供者
 * 简化模型获取方式，直接通过 Spring 注入
 *
 * 使用示例:
 * <pre>
 * // 注入提供者
 * @Autowired
 * private ModelProvider modelProvider;
 *
 * // 获取默认模型
 * TextModel text = modelProvider.getTextModel();
 *
 * // 获取指定模型
 * EmbeddingModel embedding = modelProvider.getEmbeddingModel("huggingfaceEmbeddingModel");
 * </pre>
 */
@Slf4j
@Component
public class ModelProvider {

    // ==================== 默认模型注入（使用 @Primary） ====================

    @Autowired
    @Qualifier("textModel")
    private TextModel defaultTextModel;

    @Autowired
    @Qualifier("embeddingModel")
    private EmbeddingModel defaultEmbeddingModel;

    @Autowired
    @Qualifier("imageModel")
    private ImageModel defaultImageModel;

    @Autowired
    @Qualifier("rerankModel")
    private RerankModel defaultRerankModel;

    // ==================== 指定模型注入 ====================

    @Autowired
    @Qualifier("huggingfaceTextModel")
    private TextModel huggingfaceTextModel;

    @Autowired
    @Qualifier("huggingfaceEmbeddingModel")
    private EmbeddingModel huggingfaceEmbeddingModel;

    @Autowired
    @Qualifier("huggingfaceImageModel")
    private ImageModel huggingfaceImageModel;

    @Autowired
    @Qualifier("huggingfaceRerankModel")
    private RerankModel huggingfaceRerankModel;

    @Autowired
    @Qualifier("ggufTextModel")
    private TextModel ggufTextModel;

    @Autowired
    @Qualifier("ggufEmbeddingModel")
    private EmbeddingModel ggufEmbeddingModel;

    @Autowired
    @Qualifier("onnxTextModel")
    private TextModel onnxTextModel;

    @Autowired
    @Qualifier("onnxEmbeddingModel")
    private EmbeddingModel onnxEmbeddingModel;

    // ==================== 默认模型获取方法 ====================

    public TextModel getTextModel() {
        return defaultTextModel;
    }

    public EmbeddingModel getEmbeddingModel() {
        return defaultEmbeddingModel;
    }

    public ImageModel getImageModel() {
        return defaultImageModel;
    }

    public RerankModel getRerankModel() {
        return defaultRerankModel;
    }

    // ==================== 指定模型获取方法 ====================

    public Optional<TextModel> getTextModel(String beanName) {
        return switch (beanName) {
            case "huggingfaceTextModel" -> Optional.of(huggingfaceTextModel);
            case "ggufTextModel" -> Optional.of(ggufTextModel);
            case "onnxTextModel" -> Optional.of(onnxTextModel);
            default -> Optional.empty();
        };
    }

    public Optional<EmbeddingModel> getEmbeddingModel(String beanName) {
        return switch (beanName) {
            case "huggingfaceEmbeddingModel" -> Optional.of(huggingfaceEmbeddingModel);
            case "ggufEmbeddingModel" -> Optional.of(ggufEmbeddingModel);
            case "onnxEmbeddingModel" -> Optional.of(onnxEmbeddingModel);
            default -> Optional.empty();
        };
    }

    public Optional<ImageModel> getImageModel(String beanName) {
        return "huggingfaceImageModel".equals(beanName) ? Optional.of(huggingfaceImageModel) : Optional.empty();
    }

    public Optional<RerankModel> getRerankModel(String beanName) {
        return "huggingfaceRerankModel".equals(beanName) ? Optional.of(huggingfaceRerankModel) : Optional.empty();
    }

    // ==================== 通用模型获取方法 ====================

    @SuppressWarnings("unchecked")
    public <T extends DjlModel> Optional<T> getModel(String beanName, Class<T> modelType) {
        Object model = switch (beanName) {
            case "huggingfaceTextModel" -> huggingfaceTextModel;
            case "huggingfaceEmbeddingModel" -> huggingfaceEmbeddingModel;
            case "huggingfaceImageModel" -> huggingfaceImageModel;
            case "huggingfaceRerankModel" -> huggingfaceRerankModel;
            case "ggufTextModel" -> ggufTextModel;
            case "ggufEmbeddingModel" -> ggufEmbeddingModel;
            case "onnxTextModel" -> onnxTextModel;
            case "onnxEmbeddingModel" -> onnxEmbeddingModel;
            default -> null;
        };
        if (model != null && modelType.isInstance(model)) {
            return Optional.of((T) model);
        }
        return Optional.empty();
    }
}