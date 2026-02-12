package com.shuanglin.framework.onebot11.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OneBot11Exception 单元测试
 *
 * @author Shuanglin
 * @since 1.0
 */
class OneBot11ExceptionTest {

    @Test
    @DisplayName("创建异常 - 仅消息")
    void createExceptionWithMessage() {
        // Given
        String message = "Test error message";

        // When
        OneBot11Exception exception = new OneBot11Exception(message);

        // Then
        assertEquals(message, exception.getMessage());
        assertNull(exception.getErrorCode());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("创建异常 - 带错误代码")
    void createExceptionWithErrorCode() {
        // Given
        String message = "Test error with code";
        Integer errorCode = 1001;

        // When
        OneBot11Exception exception = new OneBot11Exception(message, errorCode);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(errorCode, exception.getErrorCode());
    }

    @Test
    @DisplayName("创建异常 - 带原始异常")
    void createExceptionWithCause() {
        // Given
        String message = "Test error with cause";
        Throwable cause = new RuntimeException("Original error");

        // When
        OneBot11Exception exception = new OneBot11Exception(message, cause);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertNull(exception.getErrorCode());
    }

    @Test
    @DisplayName("创建异常 - 完整参数")
    void createExceptionWithAllParams() {
        // Given
        String message = "Complete error";
        Throwable cause = new RuntimeException("Original");
        Integer errorCode = 500;

        // When
        OneBot11Exception exception = new OneBot11Exception(message, cause, errorCode);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertEquals(errorCode, exception.getErrorCode());
    }

    @Test
    @DisplayName("toString 包含错误代码")
    void toStringIncludesErrorCode() {
        // Given
        OneBot11Exception exception = new OneBot11Exception("Error", 123);

        // When
        String result = exception.toString();

        // Then
        assertTrue(result.contains("OneBot11Exception"));
        assertTrue(result.contains("Error"));
        assertTrue(result.contains("123"));
    }

    @Test
    @DisplayName("toString 不包含 null 错误代码")
    void toStringWithoutNullErrorCode() {
        // Given
        OneBot11Exception exception = new OneBot11Exception("Error");

        // When
        String result = exception.toString();

        // Then
        assertTrue(result.contains("OneBot11Exception"));
        assertTrue(result.contains("Error"));
        assertFalse(result.contains("code:"));
    }
}
