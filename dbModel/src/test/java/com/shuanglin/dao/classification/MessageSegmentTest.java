package com.shuanglin.dao.classification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 消息段测试
 */
@DisplayName("消息段测试")
class MessageSegmentTest {

    @Test
    @DisplayName("应该能使用Builder创建文本段")
    void shouldCreateTextSegmentUsingBuilder() {
        // Given
        String type = "text";
        Map<String, Object> data = Map.of("text", "Hello World");

        // When
        MessageSegment segment = MessageSegment.builder()
                .type(type)
                .data(data)
                .build();

        // Then
        assertNotNull(segment);
        assertEquals(type, segment.getType());
        assertEquals(data, segment.getData());
    }

    @Test
    @DisplayName("应该能使用静态方法创建文本段")
    void shouldCreateTextSegmentUsingStaticMethod() {
        // Given
        String text = "Hello World";

        // When
        MessageSegment segment = MessageSegment.text(text);

        // Then
        assertNotNull(segment);
        assertEquals("text", segment.getType());
        assertEquals(text, segment.getData().get("text"));
    }

    @Test
    @DisplayName("应该能使用静态方法创建@用户段")
    void shouldCreateAtSegmentUsingStaticMethod() {
        // Given
        Long userId = 123456789L;

        // When
        MessageSegment segment = MessageSegment.at(userId);

        // Then
        assertNotNull(segment);
        assertEquals("at", segment.getType());
        assertEquals(userId, segment.getData().get("qq"));
    }

    @Test
    @DisplayName("应该能使用静态方法创建图片段")
    void shouldCreateImageSegmentUsingStaticMethod() {
        // Given
        String url = "http://example.com/image.jpg";

        // When
        MessageSegment segment = MessageSegment.image(url);

        // Then
        assertNotNull(segment);
        assertEquals("image", segment.getType());
        assertEquals(url, segment.getData().get("url"));
    }

    @Test
    @DisplayName("应该支持无参构造")
    void shouldSupportNoArgsConstructor() {
        // When
        MessageSegment segment = new MessageSegment();

        // Then
        assertNotNull(segment);
        assertNull(segment.getType());
        assertNull(segment.getData());
    }

    @Test
    @DisplayName("应该支持全参构造")
    void shouldSupportAllArgsConstructor() {
        // Given
        String type = "custom";
        Map<String, Object> data = Map.of("key", "value");

        // When
        MessageSegment segment = new MessageSegment(type, data);

        // Then
        assertEquals(type, segment.getType());
        assertEquals(data, segment.getData());
    }

    @Test
    @DisplayName("应该能正确存储复杂数据")
    void shouldStoreComplexData() {
        // Given
        Map<String, Object> complexData = Map.of(
                "url", "http://example.com/image.jpg",
                "width", 1920,
                "height", 1080,
                "size", 1024000
        );

        // When
        MessageSegment segment = MessageSegment.builder()
                .type("image")
                .data(complexData)
                .build();

        // Then
        assertEquals(1920, segment.getData().get("width"));
        assertEquals(1080, segment.getData().get("height"));
    }
}
