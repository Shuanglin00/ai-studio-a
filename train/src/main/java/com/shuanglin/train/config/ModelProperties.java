package com.shuanglin.train.config;

import com.shuanglin.train.model.framework.ModelFramework;
import com.shuanglin.train.model.type.ModelType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 模型配置属性类
 * 用于加载 application.yaml 中的模型配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "train.model")
public class ModelProperties {

    /**
     * 是否启用模型缓存
     */
    private boolean cacheEnabled = true;

    /**
     * 模型缓存目录
     */
    private String cacheDir = "./model-cache";

    /**
     * 最大模型数量
     */
    private int maxModels = 10;

    /**
     * 默认模型配置
     */
    private Map<String, ModelConfig> models = new HashMap<>();

    /**
     * 单个模型配置
     */
    @Data
    public static class ModelConfig {
        /**
         * 模型名称/标识符
         */
        private String name;

        /**
         * 模型类型: textModel, embeddingModel, imageModel...
         */
        private ModelType type;

        /**
         * 模型框架: huggingface, gguf, onnx...
         */
        private ModelFramework framework;

        /**
         * 模型路径或仓库 ID
         */
        private String modelPath;

        /**
         * 模型版本/标签
         */
        private String version;

        /**
         * 设备: cpu, gpu, auto
         */
        private String device = "auto";

        /**
         * 批量大小
         */
        private int batchSize = 1;

        /**
         * 线程数
         */
        private int threads = 4;

        /**
         * 模型参数配置
         */
        private Map<String, Object> parameters = new HashMap<>();
    }
}
