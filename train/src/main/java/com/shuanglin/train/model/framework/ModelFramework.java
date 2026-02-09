package com.shuanglin.train.model.framework;

import java.util.Arrays;
import java.util.List;

/**
 * 模型框架枚举
 * 定义支持的模型框架/格式
 */
public enum ModelFramework {
    /**
     * HuggingFace Transformers 格式
     */
    HUGGINGFACE("huggingface", "HuggingFace Transformers", Arrays.asList("pt", "safetensors")),

    /**
     * GGUF 格式 (llama.cpp)
     */
    GGUF("gguf", "GGUF (llama.cpp)", Arrays.asList("gguf")),

    /**
     * ONNX 格式
     */
    ONNX("onnx", "ONNX Runtime", Arrays.asList("onnx", "pb")),

    /**
     * TorchScript 格式
     */
    TORCH_SCRIPT("torchscript", "TorchScript", Arrays.asList("pt", "pth")),

    /**
     * RKNN (瑞芯微) 格式
     */
    RKNN("rknn", "RKNN (瑞芯微)", Arrays.asList("rknn")),

    /**
     * Apache TVM 格式
     */
    TVM("tvm", "Apache TVM", Arrays.asList("tvm")),

    /**
     * TensorRT 格式 (NVIDIA)
     */
    TENSOR_RT("tensorrt", "TensorRT (NVIDIA)", Arrays.asList("engine", "plan")),

    /**
     * PaddlePaddle 格式
     */
    PADDLE("paddle", "PaddlePaddle", Arrays.asList("pdmodel", "pdiparams")),

    /**
     * TensorFlow 格式
     */
    TENSOR_FLOW("tensorflow", "TensorFlow", Arrays.asList("pb", "h5", "ckpt")),

    /**
     * RWKV 格式
     */
    RWKV("rwkv", "RWKV", Arrays.asList("pth", "safetensors"));

    private final String code;
    private final String description;
    private final List<String> supportedExtensions;

    ModelFramework(String code, String description, List<String> supportedExtensions) {
        this.code = code;
        this.description = description;
        this.supportedExtensions = supportedExtensions;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getSupportedExtensions() {
        return supportedExtensions;
    }

    /**
     * 根据 code 获取枚举
     */
    public static ModelFramework fromCode(String code) {
        for (ModelFramework framework : values()) {
            if (framework.code.equalsIgnoreCase(code)) {
                return framework;
            }
        }
        throw new IllegalArgumentException("未知的模型框架: " + code);
    }

    /**
     * 根据文件扩展名推断框架
     */
    public static ModelFramework fromExtension(String extension) {
        for (ModelFramework framework : values()) {
            if (framework.supportedExtensions.contains(extension.toLowerCase())) {
                return framework;
            }
        }
        return null;
    }
}
