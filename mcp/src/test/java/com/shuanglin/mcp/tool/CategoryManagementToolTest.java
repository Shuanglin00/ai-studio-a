package com.shuanglin.mcp.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shuanglin.dao.classification.MessageCategoryDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 分类管理工具测试
 */
@DisplayName("分类管理工具测试")
class CategoryManagementToolTest {

    private CategoryManagementTool tool;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        tool = new CategoryManagementTool(null, objectMapper);
    }

    @Test
    @DisplayName("应该能列出所有分类")
    void shouldListAllCategories() {
        // When
        String result = tool.listCategories();

        // Then
        assertNotNull(result);
        assertFalse(result.isBlank());
    }

    @Test
    @DisplayName("列出的分类应该包含默认分类")
    void shouldContainDefaultCategories() {
        // When
        String result = tool.listCategories();

        // Then
        assertTrue(result.contains("MEME") || result.contains("meme"));
        assertTrue(result.contains("HELL_JOKE") || result.contains("hell-joke"));
    }

    @Test
    @DisplayName("应该能验证有效的分类编码")
    void shouldValidateValidCategoryCode() {
        // Given
        String validCode = "meme";

        // When
        boolean isValid = tool.isValidCategory(validCode);

        // Then
        assertTrue(isValid);
    }

    @Test
    @DisplayName("应该能验证无效的分类编码")
    void shouldInvalidateInvalidCategoryCode() {
        // Given
        String invalidCode = "invalid-category";

        // When
        boolean isValid = tool.isValidCategory(invalidCode);

        // Then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("应该能获取分类详情")
    void shouldGetCategoryDetails() {
        // Given
        String categoryCode = "meme";

        // When
        String result = tool.getCategoryDetails(categoryCode);

        // Then
        assertNotNull(result);
        assertFalse(result.isBlank());
        assertTrue(result.contains("meme") || result.contains("MEME"));
    }

    @Test
    @DisplayName("获取不存在的分类详情应该返回错误")
    void shouldReturnErrorForNonExistentCategory() {
        // Given
        String invalidCode = "non-existent";

        // When
        String result = tool.getCategoryDetails(invalidCode);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("error") || result.contains("not found"));
    }

    @Test
    @DisplayName("应该能获取分类描述")
    void shouldGetCategoryDescription() {
        // Given
        String categoryCode = "hell-joke";

        // When
        String description = tool.getCategoryDescription(categoryCode);

        // Then
        assertNotNull(description);
        assertFalse(description.isBlank());
    }

    @Test
    @DisplayName("应该能判断敏感分类")
    void shouldIdentifySensitiveCategories() {
        // Given
        String sensitiveCode = "hell-joke";
        String normalCode = "meme";

        // When & Then
        assertTrue(tool.isSensitiveCategory(sensitiveCode));
        assertFalse(tool.isSensitiveCategory(normalCode));
    }

    @Test
    @DisplayName("应该能获取所有敏感分类")
    void shouldGetAllSensitiveCategories() {
        // When
        List<String> sensitiveCategories = tool.getSensitiveCategories();

        // Then
        assertNotNull(sensitiveCategories);
        assertFalse(sensitiveCategories.isEmpty());
        assertTrue(sensitiveCategories.contains("hell-joke"));
        assertTrue(sensitiveCategories.contains("regional-black"));
    }

    @Test
    @DisplayName("空编码应该返回无效")
    void emptyCodeShouldBeInvalid() {
        // Given
        String emptyCode = "";
        String nullCode = null;

        // Then
        assertFalse(tool.isValidCategory(emptyCode));
        assertFalse(tool.isValidCategory(nullCode));
    }

    @Test
    @DisplayName("应该能获取分类优先级")
    void shouldGetCategoryPriority() {
        // Given
        String categoryCode = "meme";

        // When
        int priority = tool.getCategoryPriority(categoryCode);

        // Then
        assertTrue(priority > 0);
    }

    @Test
    @DisplayName("无效分类的优先级应该返回-1")
    void invalidCategoryShouldReturnNegativePriority() {
        // Given
        String invalidCode = "invalid";

        // When
        int priority = tool.getCategoryPriority(invalidCode);

        // Then
        assertEquals(-1, priority);
    }
}
