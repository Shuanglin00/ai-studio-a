package com.shuanglin.framework.onebot11.exception;

/**
 * OneBot 11 基础异常类
 * 所有 OneBot 11 相关异常的基类
 *
 * @author Shuanglin
 * @since 1.0
 */
public class OneBot11Exception extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误代码
     */
    private final Integer errorCode;

    /**
     * 创建异常
     *
     * @param message 错误消息
     */
    public OneBot11Exception(String message) {
        super(message);
        this.errorCode = null;
    }

    /**
     * 创建异常
     *
     * @param message 错误消息
     * @param cause   原始异常
     */
    public OneBot11Exception(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = null;
    }

    /**
     * 创建异常
     *
     * @param message   错误消息
     * @param errorCode 错误代码
     */
    public OneBot11Exception(String message, Integer errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * 创建异常
     *
     * @param message   错误消息
     * @param cause     原始异常
     * @param errorCode 错误代码
     */
    public OneBot11Exception(String message, Throwable cause, Integer errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * 获取错误代码
     *
     * @return 错误代码，可能为 null
     */
    public Integer getErrorCode() {
        return errorCode;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getSimpleName());
        sb.append(": ").append(getMessage());
        if (errorCode != null) {
            sb.append(" (code: ").append(errorCode).append(")");
        }
        return sb.toString();
    }
}
