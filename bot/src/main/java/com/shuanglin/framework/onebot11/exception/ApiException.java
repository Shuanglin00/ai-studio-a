package com.shuanglin.framework.onebot11.exception;

/**
 * API 调用异常
 * 当调用 OneBot 11 API 发生错误时抛出
 *
 * @author Shuanglin
 * @since 1.0
 */
public class ApiException extends OneBot11Exception {

    private static final long serialVersionUID = 1L;

    /**
     * HTTP 状态码
     */
    private final Integer httpStatus;

    /**
     * API 端点
     */
    private final String endpoint;

    /**
     * 创建 API 异常
     *
     * @param message 错误消息
     */
    public ApiException(String message) {
        super(message);
        this.httpStatus = null;
        this.endpoint = null;
    }

    /**
     * 创建 API 异常
     *
     * @param message 错误消息
     * @param cause   原始异常
     */
    public ApiException(String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = null;
        this.endpoint = null;
    }

    /**
     * 创建 API 异常
     *
     * @param message    错误消息
     * @param errorCode  错误代码
     * @param httpStatus HTTP 状态码
     */
    public ApiException(String message, Integer errorCode, Integer httpStatus) {
        super(message, errorCode);
        this.httpStatus = httpStatus;
        this.endpoint = null;
    }

    /**
     * 创建 API 异常
     *
     * @param message    错误消息
     * @param errorCode  错误代码
     * @param httpStatus HTTP 状态码
     * @param endpoint   API 端点
     */
    public ApiException(String message, Integer errorCode, Integer httpStatus, String endpoint) {
        super(message, errorCode);
        this.httpStatus = httpStatus;
        this.endpoint = endpoint;
    }

    /**
     * 创建 API 异常
     *
     * @param message    错误消息
     * @param cause      原始异常
     * @param errorCode  错误代码
     * @param httpStatus HTTP 状态码
     * @param endpoint   API 端点
     */
    public ApiException(String message, Throwable cause, Integer errorCode, Integer httpStatus, String endpoint) {
        super(message, cause, errorCode);
        this.httpStatus = httpStatus;
        this.endpoint = endpoint;
    }

    /**
     * 获取 HTTP 状态码
     *
     * @return HTTP 状态码，可能为 null
     */
    public Integer getHttpStatus() {
        return httpStatus;
    }

    /**
     * 获取 API 端点
     *
     * @return API 端点，可能为 null
     */
    public String getEndpoint() {
        return endpoint;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ApiException: ");
        sb.append(getMessage());
        if (getErrorCode() != null) {
            sb.append(" (retcode: ").append(getErrorCode()).append(")");
        }
        if (httpStatus != null) {
            sb.append(" [HTTP ").append(httpStatus).append("]");
        }
        if (endpoint != null) {
            sb.append(" at ").append(endpoint);
        }
        return sb.toString();
    }
}
