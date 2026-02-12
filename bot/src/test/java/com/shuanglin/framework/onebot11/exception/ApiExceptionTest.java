package com.shuanglin.framework.onebot11.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ApiException 单元测试
 *
 * @author Shuanglin
 * @since 1.0
 */
class ApiExceptionTest {

    @Test
    @DisplayName("创建异常 - 仅消息")
    void createExceptionWithMessage() {
        // Given
        String message = "API call failed";

        // When
        ApiException exception = new ApiException(message);

        // Then
        assertEquals(message, exception.getMessage());
        assertNull(exception.getHttpStatus());
        assertNull(exception.getEndpoint());
    }

    @Test
    @DisplayName("创建异常 - 带错误代码和 HTTP 状态")
    void createExceptionWithErrorCodeAndStatus() {
        // Given
        String message = "Not found";
        Integer errorCode = 404;
        Integer httpStatus = 404;

        // When
        ApiException exception = new ApiException(message, errorCode, httpStatus);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(httpStatus, exception.getHttpStatus());
    }

    @Test
    @DisplayName("创建异常 - 完整参数")
    void createExceptionWithAllParams() {
        // Given
        String message = "Server error";
        Integer errorCode = 500;
        Integer httpStatus = 500;
        String endpoint = "/test/api";

        // When
        ApiException exception = new ApiException(message, errorCode, httpStatus, endpoint);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(httpStatus, exception.getHttpStatus());
        assertEquals(endpoint, exception.getEndpoint());
    }

    @Test
    @DisplayName("toString 包含所有信息")
    void toStringIncludesAllInfo() {
        // Given
        ApiException exception = new ApiException(
                "Error", 100, 500, "/api/test"
        );

        // When
        String result = exception.toString();

        // Then
        assertTrue(result.contains("ApiException"));
        assertTrue(result.contains("Error"));
        assertTrue(result.contains("100"));
        assertTrue(result.contains("500"));
        assertTrue(result.contains("/api/test"));
    }

    @Test
    @DisplayName("toString 不包含 null 值")
    void toStringWithoutNullValues() {
        // Given
        ApiException exception = new ApiException("Simple error");

        // When
        String result = exception.toString();

        // Then
        assertTrue(result.contains("ApiException"));
        assertTrue(result.contains("Simple error"));
        assertFalse(result.contains("retcode:"));
        assertFalse(result.contains("HTTP"));
    }
}
