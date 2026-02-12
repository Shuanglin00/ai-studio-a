package com.shuanglin.framework.onebot11.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OneBot11Properties 单元测试
 *
 * @author Shuanglin
 * @since 1.0
 */
class OneBot11PropertiesTest {

    @Test
    @DisplayName("默认配置值")
    void defaultValues() {
        // When
        OneBot11Properties properties = new OneBot11Properties();

        // Then
        assertEquals("http://127.0.0.1:3000", properties.getBaseUrl());
        assertNull(properties.getAccessToken());
        assertEquals(5000L, properties.getConnectTimeout());
        assertEquals(10000L, properties.getReadTimeout());
        assertEquals(10000L, properties.getWriteTimeout());
        assertTrue(properties.getRetryEnabled());
        assertEquals(3, properties.getMaxRetries());
        assertEquals(1000L, properties.getRetryDelay());
        assertEquals(2.0, properties.getRetryMultiplier());
    }

    @Test
    @DisplayName("自定义配置值")
    void customValues() {
        // Given
        OneBot11Properties properties = new OneBot11Properties();

        // When
        properties.setBaseUrl("http://localhost:8080");
        properties.setAccessToken("test-token");
        properties.setConnectTimeout(3000L);
        properties.setReadTimeout(5000L);
        properties.setWriteTimeout(5000L);
        properties.setRetryEnabled(false);
        properties.setMaxRetries(5);
        properties.setRetryDelay(2000L);
        properties.setRetryMultiplier(1.5);

        // Then
        assertEquals("http://localhost:8080", properties.getBaseUrl());
        assertEquals("test-token", properties.getAccessToken());
        assertEquals(3000L, properties.getConnectTimeout());
        assertEquals(5000L, properties.getReadTimeout());
        assertEquals(5000L, properties.getWriteTimeout());
        assertFalse(properties.getRetryEnabled());
        assertEquals(5, properties.getMaxRetries());
        assertEquals(2000L, properties.getRetryDelay());
        assertEquals(1.5, properties.getRetryMultiplier());
    }
}
