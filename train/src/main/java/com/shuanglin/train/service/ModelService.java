package com.shuanglin.train.service;

import com.shuanglin.train.model.base.DjlModel;
import com.shuanglin.train.model.type.*;

import java.util.List;
import java.util.Optional;

/**
 * DJL 模型服务接口
 * AI 模块通过注入此服务来访问配置好的模型
 *
 * 使用示例:
 * <pre>
 * {@code
 * @Autowired
 * private ModelService modelService;
 *
 * // 获取文本生成模型
 * TextModel textModel = modelService.getTextModel("qwen-huggingface");
 *
 * // 获取嵌入模型
 * EmbeddingModel embeddingModel = modelService.getEmbeddingModel("bge-onnx");
 *
 * // 生成文本
 * String response = textModel.generate("你好");
 * }
 * </pre>
 */
public interface ModelService {

    // ==================== TextModel 获取方法 ====================

    /**
     * 根据名称获取文本生成模型
     */
    Optional<TextModel> getTextModel(String modelName);

    /**
     * 获取默认文本生成模型
     */
    Optional<TextModel> getDefaultTextModel();

    // ==================== EmbeddingModel 获取方法 ====================

    /**
     * 根据名称获取嵌入模型
     */
    Optional<EmbeddingModel> getEmbeddingModel(String modelName);

    /**
     * 获取默认嵌入模型
     */
    Optional<EmbeddingModel> getDefaultEmbeddingModel();

    // ==================== ImageModel 获取方法 ====================

    /**
     * 根据名称获取图像生成模型
     */
    Optional<ImageModel> getImageModel(String modelName);

    /**
     * 获取默认图像生成模型
     */
    Optional<ImageModel> getDefaultImageModel();

    // ==================== RerankModel 获取方法 ====================

    /**
     * 根据名称获取重排序模型
     */
    Optional<RerankModel> getRerankModel(String modelName);

    /**
     * 获取默认重排序模型
     */
    Optional<RerankModel> getDefaultRerankModel();

    // ==================== 通用方法 ====================

    /**
     * 根据名称和类型获取任意模型
     */
    @SuppressWarnings("unchecked")
    <T extends DjlModel> Optional<T> getModel(String modelName, Class<T> modelType);

    /**
     * 获取所有已加载的模型名称
     */
    List<String> getLoadedModelNames();

    /**
     * 检查模型是否已加载
     */
    boolean isModelLoaded(String modelName, Class<? extends DjlModel> modelType);

    /**
     * 重新加载模型
     */
    boolean reloadModel(String modelName, Class<? extends DjlModel> modelType);

    /**
     * 卸载模型
     */
    boolean unloadModel(String modelName);
}
