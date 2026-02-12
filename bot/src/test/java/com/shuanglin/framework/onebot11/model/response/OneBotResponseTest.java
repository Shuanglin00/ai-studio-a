package com.shuanglin.framework.onebot11.model.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OneBotResponse 单元测试
 *
 * @author Shuanglin
 * @since 1.0
 */
class OneBotResponseTest {

    @Test
    @DisplayName("成功响应判断")
    void successResponse() {
        // Given
        OneBotResponse<String> response = new OneBotResponse<>();
        response.setStatus("ok");
        response.setRetcode(0);
        response.setData("test data");

        // Then
        assertTrue(response.isSuccess());
        assertFalse(response.isFailed());
        assertFalse(response.isAsync());
    }

    @Test
    @DisplayName("失败响应判断")
    void failedResponse() {
        // Given
        OneBotResponse<String> response = new OneBotResponse<>();
        response.setStatus("failed");
        response.setRetcode(100);
        response.setMessage("Error message");

        // Then
        assertFalse(response.isSuccess());
        assertTrue(response.isFailed());
        assertFalse(response.isAsync());
    }

    @Test
    @DisplayName("非零返回码视为失败")
    void nonZeroRetcodeIsFailed() {
        // Given
        OneBotResponse<String> response = new OneBotResponse<>();
        response.setStatus("ok");
        response.setRetcode(1); // 非零

        // Then
        assertFalse(response.isSuccess());
        assertTrue(response.isFailed());
    }

    @Test
    @DisplayName("异步响应判断")
    void asyncResponse() {
        // Given
        OneBotResponse<String> response = new OneBotResponse<>();
        response.setStatus("async");
        response.setRetcode(0);

        // Then
        assertFalse(response.isSuccess());
        assertFalse(response.isFailed());
        assertTrue(response.isAsync());
    }

    @Test
    @DisplayName("获取错误消息 - 优先使用 wording")
    void getErrorMessagePreferWording() {
        // Given
        OneBotResponse<String> response = new OneBotResponse<>();
        response.setMessage("Technical error");
        response.setWording("User friendly error");

        // Then
        assertEquals("User friendly error", response.getErrorMessage());
    }

    @Test
    @DisplayName("获取错误消息 - 使用 message 当 wording 为空")
    void getErrorMessageFallbackToMessage() {
        // Given
        OneBotResponse<String> response = new OneBotResponse<>();
        response.setMessage("Error message");
        response.setWording("");

        // Then
        assertEquals("Error message", response.getErrorMessage());
    }

    @Test
    @DisplayName("获取错误消息 - 使用 message 当 wording 为 null")
    void getErrorMessageFallbackWhenWordingNull() {
        // Given
        OneBotResponse<String> response = new OneBotResponse<>();
        response.setMessage("Error message");
        response.setWording(null);

        // Then
        assertEquals("Error message", response.getErrorMessage());
    }

    @Test
    @DisplayName("Getter 和 Setter 工作正常")
    void gettersAndSetters() {
        // Given
        OneBotResponse<String> response = new OneBotResponse<>();

        // When
        response.setStatus("ok");
        response.setRetcode(0);
        response.setData("data");
        response.setMessage("msg");
        response.setWording("wording");
        response.setEcho("echo123");

        // Then
        assertEquals("ok", response.getStatus());
        assertEquals(0, response.getRetcode());
        assertEquals("data", response.getData());
        assertEquals("msg", response.getMessage());
        assertEquals("wording", response.getWording());
        assertEquals("echo123", response.getEcho());
    }
}
