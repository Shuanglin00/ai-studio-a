package com.shuanglin.train.exception;

/**
 * 模型调用异常
 */
public class ModelException extends RuntimeException {

    private final String modelId;
    private final ErrorType errorType;

    /**
     * 错误类型枚举
     */
    public enum ErrorType {
        LOAD_FAILED("模型加载失败"),
        UNLOAD_FAILED("模型卸载失败"),
        PREDICTION_FAILED("预测失败"),
        MODEL_NOT_FOUND("模型不存在"),
        RESOURCE_EXHAUSTED("资源耗尽"),
        TIMEOUT("超时"),
        UNKNOWN("未知错误");

        private final String description;

        ErrorType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public ModelException(String message) {
        super(message);
        this.modelId = "unknown";
        this.errorType = ErrorType.UNKNOWN;
    }

    public ModelException(String modelId, String message) {
        super(message);
        this.modelId = modelId;
        this.errorType = ErrorType.UNKNOWN;
    }

    public ModelException(String modelId, String message, ErrorType errorType) {
        super(message);
        this.modelId = modelId;
        this.errorType = errorType;
    }

    public ModelException(String modelId, String message, Throwable cause) {
        super(message, cause);
        this.modelId = modelId;
        this.errorType = ErrorType.UNKNOWN;
    }

    public ModelException(String modelId, String message, ErrorType errorType, Throwable cause) {
        super(message, cause);
        this.modelId = modelId;
        this.errorType = errorType;
    }

    public String getModelId() {
        return modelId;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    public boolean isRetryable() {
        return errorType == ErrorType.TIMEOUT ||
                errorType == ErrorType.RESOURCE_EXHAUSTED ||
                errorType == ErrorType.PREDICTION_FAILED;
    }

    @Override
    public String toString() {
        return String.format("ModelException[%s][%s]: %s",
                modelId, errorType.getDescription(), getMessage());
    }
}