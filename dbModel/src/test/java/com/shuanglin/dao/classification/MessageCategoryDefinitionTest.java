package com.shuanglin.dao.classification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 分类标签定义实体测试
 */
@DisplayName("分类标签定义实体测试")
class MessageCategoryDefinitionTest {

    @Test
    @DisplayName("应该能使用Builder创建对象")
    void shouldCreateUsingBuilder() {
        // Given
        String code = "MEME";
        String name = "弔图";
        String description = "搞笑图片、表情包";

        // When
        MessageCategoryDefinition definition = MessageCategoryDefinition.builder()
                .id("def-001")
                .code(code)
                .name(name)
                .description(description)
                .priority(1)
                .enabled(true)
                .build();

        // Then
        assertNotNull(definition);
        assertEquals("def-001", definition.getId());
        assertEquals(code, definition.getCode());
        assertEquals(name, definition.getName());
        assertEquals(description, definition.getDescription());
        assertEquals(1, definition.getPriority());
        assertTrue(definition.getEnabled());
    }

    @Test
    @DisplayName("应该能设置关键词列表")
    void shouldSetKeywords() {
        // Given
        List<String> keywords = Arrays.asList("哈哈", "笑死", "表情包", "搞笑");

        // When
        MessageCategoryDefinition definition = MessageCategoryDefinition.builder()
                .id("def-001")
                .code("MEME")
                .name("弔图")
                .keywords(keywords)
                .build();

        // Then
        assertNotNull(definition.getKeywords());
        assertEquals(4, definition.getKeywords().size());
        assertTrue(definition.getKeywords().contains("哈哈"));
    }

    @Test
    @DisplayName("应该支持禁用标签")
    void shouldSupportDisabling() {
        // When
        MessageCategoryDefinition definition = MessageCategoryDefinition.builder()
                .id("def-001")
                .code("TEST")
                .name("测试")
                .enabled(false)
                .build();

        // Then
        assertFalse(definition.getEnabled());
    }

    @Test
    @DisplayName("应该支持无参构造")
    void shouldSupportNoArgsConstructor() {
        // When
        MessageCategoryDefinition definition = new MessageCategoryDefinition();

        // Then
        assertNotNull(definition);
    }

    @Test
    @DisplayName("优先级应该为正整数")
    void priorityShouldBePositive() {
        // Given
        int highPriority = 1;
        int lowPriority = 10;

        // When
        MessageCategoryDefinition high = MessageCategoryDefinition.builder()
                .priority(highPriority)
                .build();
        MessageCategoryDefinition low = MessageCategoryDefinition.builder()
                .priority(lowPriority)
                .build();

        // Then
        assertEquals(highPriority, high.getPriority());
        assertEquals(lowPriority, low.getPriority());
    }
}
