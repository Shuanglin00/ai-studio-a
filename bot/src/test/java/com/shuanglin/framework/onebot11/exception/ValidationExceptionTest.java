package com.shuanglin.framework.onebot11.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ValidationException 单元测试
 *
 * @author Shuanglin
 * @since 1.0
 */
class ValidationExceptionTest {

    @Test
    @DisplayName("创建异常 - 仅消息")
    void createExceptionWithMessage() {
        // Given
        String message = "Invalid input";

        // When
        ValidationException exception = new ValidationException(message);

        // Then
        assertEquals(message, exception.getMessage());
        assertNull(exception.getFieldName());
    }

    @Test
    @DisplayName("创建异常 - 带字段名")
    void createExceptionWithFieldName() {
        // Given
        String message = "Cannot be empty";
        String fieldName = "username";

        // When
        ValidationException exception = new ValidationException(message, fieldName);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(fieldName, exception.getFieldName());
    }

    @Test
    @DisplayName("创建异常 - 带字段名和原始异常")
    void createExceptionWithFieldNameAndCause() {
        // Given
        String message = "Parse failed";
        String fieldName = "age";
        Throwable cause = new NumberFormatException("Not a number");

        // When
        ValidationException exception = new ValidationException(message, fieldName, cause);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(fieldName, exception.getFieldName());
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("toString 包含字段名")
    void toStringIncludesFieldName() {
        // Given
        ValidationException exception = new ValidationException("Required", "email");

        // When
        String result = exception.toString();

        // Then
        assertTrue(result.contains("ValidationException"));
        assertTrue(result.contains("[email]"));
        assertTrue(result.contains("Required"));
    }

    @Test
    @DisplayName("toString 不包含 null 字段名")
    void toStringWithoutNullFieldName() {
        // Given
        ValidationException exception = new ValidationException("Error");

        // When
        String result = exception.toString();

        // Then
        assertTrue(result.contains("ValidationException"));
        assertTrue(result.contains("Error"));
        assertFalse(result.contains("["));
    }
}
