package com.shuanglin.framework.onebot11.exception;

/**
 * 参数校验异常
 * 当请求参数校验失败时抛出
 *
 * @author Shuanglin
 * @since 1.0
 */
public class ValidationException extends OneBot11Exception {

    private static final long serialVersionUID = 1L;

    /**
     * 字段名称
     */
    private final String fieldName;

    /**
     * 创建校验异常
     *
     * @param message 错误消息
     */
    public ValidationException(String message) {
        super(message);
        this.fieldName = null;
    }

    /**
     * 创建校验异常
     *
     * @param message   错误消息
     * @param fieldName 字段名称
     */
    public ValidationException(String message, String fieldName) {
        super(message);
        this.fieldName = fieldName;
    }

    /**
     * 创建校验异常
     *
     * @param message   错误消息
     * @param fieldName 字段名称
     * @param cause     原始异常
     */
    public ValidationException(String message, String fieldName, Throwable cause) {
        super(message, cause);
        this.fieldName = fieldName;
    }

    /**
     * 获取字段名称
     *
     * @return 字段名称，可能为 null
     */
    public String getFieldName() {
        return fieldName;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ValidationException: ");
        if (fieldName != null) {
            sb.append("[").append(fieldName).append("] ");
        }
        sb.append(getMessage());
        return sb.toString();
    }
}
