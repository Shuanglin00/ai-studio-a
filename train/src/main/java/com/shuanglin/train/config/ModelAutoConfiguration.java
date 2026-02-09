package com.shuanglin.train.config;

import com.shuanglin.train.model.framework.ModelFramework;
import com.shuanglin.train.model.type.ModelType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Map;

/**
 * 模型 Bean 注册配置
 *
 * 使用 @Bean 方式声明式注册模型：
 * - 无需 switch case，新增模型只需添加 @Bean 方法
 * - 直接通过构造函数注入 ModelProperties
 *
 * 模型使用示例:
 * <pre>
 * // 注入指定模型
 * @Autowired
 * @Qualifier("huggingfaceTextModel")
 * private TextModel qwenModel;
 *
 * // 注入默认模型
 * @Autowired
 * private TextModel textModel;
 * </pre>
 */
@Slf4j
@Configuration
public class ModelAutoConfiguration {

    /**
     * 模型配置
     */
    private final ModelProperties modelProperties;

    @Autowired
    public ModelAutoConfiguration(ModelProperties modelProperties) {
        this.modelProperties = modelProperties;
    }

    // ==================== HuggingFace 系列模型 ====================

    @Bean
    public com.shuanglin.train.model.impl.HuggingfaceTextModel huggingfaceTextModel() {
        return new com.shuanglin.train.model.impl.HuggingfaceTextModel(getModelConfig("huggingface-text"));
    }

    @Bean
    @Primary
    public com.shuanglin.train.model.type.TextModel textModel(com.shuanglin.train.model.impl.HuggingfaceTextModel impl) {
        return impl;
    }

    @Bean
    public com.shuanglin.train.model.impl.HuggingfaceEmbeddingModel huggingfaceEmbeddingModel() {
        return new com.shuanglin.train.model.impl.HuggingfaceEmbeddingModel(getModelConfig("huggingface-embedding"));
    }

    @Bean
    @Primary
    public com.shuanglin.train.model.type.EmbeddingModel embeddingModel(com.shuanglin.train.model.impl.HuggingfaceEmbeddingModel impl) {
        return impl;
    }

    @Bean
    public com.shuanglin.train.model.impl.HuggingfaceImageModel huggingfaceImageModel() {
        return new com.shuanglin.train.model.impl.HuggingfaceImageModel(getModelConfig("huggingface-image"));
    }

    @Bean
    @Primary
    public com.shuanglin.train.model.type.ImageModel imageModel(com.shuanglin.train.model.impl.HuggingfaceImageModel impl) {
        return impl;
    }

    @Bean
    public com.shuanglin.train.model.impl.HuggingfaceRerankModel huggingfaceRerankModel() {
        return new com.shuanglin.train.model.impl.HuggingfaceRerankModel(getModelConfig("huggingface-rerank"));
    }

    @Bean
    @Primary
    public com.shuanglin.train.model.type.RerankModel rerankModel(com.shuanglin.train.model.impl.HuggingfaceRerankModel impl) {
        return impl;
    }

    // ==================== GGUF 系列模型 ====================

    @Bean
    public com.shuanglin.train.model.impl.GgufTextModel ggufTextModel() {
        return new com.shuanglin.train.model.impl.GgufTextModel(getModelConfig("gguf-text"));
    }

    @Bean
    public com.shuanglin.train.model.impl.GgufEmbeddingModel ggufEmbeddingModel() {
        return new com.shuanglin.train.model.impl.GgufEmbeddingModel(getModelConfig("gguf-embedding"));
    }

    // ==================== ONNX 系列模型 ====================

    @Bean
    public com.shuanglin.train.model.impl.OnnxTextModel onnxTextModel() {
        return new com.shuanglin.train.model.impl.OnnxTextModel(getModelConfig("onnx-text"));
    }

    @Bean
    public com.shuanglin.train.model.impl.OnnxEmbeddingModel onnxEmbeddingModel() {
        return new com.shuanglin.train.model.impl.OnnxEmbeddingModel(getModelConfig("onnx-embedding"));
    }

    // ==================== 辅助方法 ====================

    /**
     * 从配置中获取指定名称的模型配置
     */
    private ModelProperties.ModelConfig getModelConfig(String name) {
        ModelProperties.ModelConfig config = modelProperties.getModels().get(name);
        if (config == null) {
            log.debug("未找到模型配置: {}，尝试根据 Bean 名称查找", name);
            // 尝试从已配置的模型中匹配
            config = findConfigByBeanName(name);
            if (config == null) {
                log.warn("未找到模型配置: {}，使用默认配置", name);
                config = createDefaultConfig(name);
            }
        }
        return config;
    }

    /**
     * 根据 Bean 名称查找配置
     * Bean 名称如: huggingfaceTextModel -> 查找包含 "huggingface" 和 "text" 的配置
     */
    private ModelProperties.ModelConfig findConfigByBeanName(String beanName) {
        String lowerBeanName = beanName.toLowerCase();
        boolean isText = lowerBeanName.contains("text");
        boolean isEmbedding = lowerBeanName.contains("embedding");
        boolean isImage = lowerBeanName.contains("image");
        boolean isRerank = lowerBeanName.contains("rerank");
        boolean isHuggingface = lowerBeanName.contains("huggingface");
        boolean isGguf = lowerBeanName.contains("gguf");
        boolean isOnnx = lowerBeanName.contains("onnx");

        for (Map.Entry<String, ModelProperties.ModelConfig> entry : modelProperties.getModels().entrySet()) {
            ModelProperties.ModelConfig config = entry.getValue();
            ModelType type = config.getType();
            ModelFramework framework = config.getFramework();

            // 检查类型匹配
            boolean typeMatch = (isText && type == ModelType.TEXT_MODEL)
                    || (isEmbedding && type == ModelType.EMBEDDING_MODEL)
                    || (isImage && type == ModelType.IMAGE_MODEL)
                    || (isRerank && type == ModelType.RERANK_MODEL);

            // 检查框架匹配
            boolean frameworkMatch = (isHuggingface && framework == ModelFramework.HUGGINGFACE)
                    || (isGguf && framework == ModelFramework.GGUF)
                    || (isOnnx && framework == ModelFramework.ONNX);

            if (typeMatch && frameworkMatch) {
                log.debug("根据 Bean 名称 {} 找到匹配配置: {}", beanName, entry.getKey());
                return config;
            }
        }
        return null;
    }

    /**
     * 创建默认配置
     */
    private ModelProperties.ModelConfig createDefaultConfig(String name) {
        ModelProperties.ModelConfig config = new ModelProperties.ModelConfig();
        config.setName(name);
        // 根据名称推断类型
        if (name.contains("text")) {
            config.setType(ModelType.TEXT_MODEL);
        } else if (name.contains("embedding")) {
            config.setType(ModelType.EMBEDDING_MODEL);
        } else if (name.contains("image")) {
            config.setType(ModelType.IMAGE_MODEL);
        } else if (name.contains("rerank")) {
            config.setType(ModelType.RERANK_MODEL);
        }
        // 根据名称推断框架
        if (name.contains("huggingface")) {
            config.setFramework(ModelFramework.HUGGINGFACE);
        } else if (name.contains("gguf")) {
            config.setFramework(ModelFramework.GGUF);
        } else if (name.contains("onnx")) {
            config.setFramework(ModelFramework.ONNX);
        }
        config.setModelPath(name);
        return config;
    }
}