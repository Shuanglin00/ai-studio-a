package com.shuanglin.mcp.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 分类管理工具附加测试
 */
@DisplayName("分类管理工具附加测试")
class CategoryManagementToolAdditionalTest {

    private CategoryManagementTool tool;

    @BeforeEach
    void setUp() {
        tool = new CategoryManagementTool(null, new ObjectMapper());
    }

    @Test
    @DisplayName("应该返回所有敏感分类")
    void shouldReturnAllSensitiveCategories() {
        // When
        List<String> sensitive = tool.getSensitiveCategories();

        // Then
        assertNotNull(sensitive);
        assertFalse(sensitive.isEmpty());
        assertTrue(sensitive.contains("hell-joke"));
        assertTrue(sensitive.contains("regional-black"));
        assertTrue(sensitive.contains("political"));
        assertTrue(sensitive.contains("nsfw"));
    }

    @Test
    @DisplayName("所有默认分类都应该能通过验证")
    void allDefaultCategoriesShouldBeValid() {
        // Given
        String[] defaultCategories = {
                "meme", "hell-joke", "regional-black", "political",
                "nsfw", "normal", "spam", "ad", "other"
        };

        // Then
        for (String category : defaultCategories) {
            assertTrue(tool.isValidCategory(category),
                    "Category " + category + " should be valid");
        }
    }

    @Test
    @DisplayName("无效分类应该返回正确的错误信息")
    void invalidCategoryShouldReturnError() {
        // Given
        String invalidCode = "non-existent-category";

        // When
        String result = tool.getCategoryDetails(invalidCode);

        // Then
        assertTrue(result.contains("error"));
        assertTrue(result.contains(invalidCode));
    }

    @Test
    @DisplayName("应该能获取所有分类的优先级")
    void shouldGetPriorityForAllCategories() {
        // Given
        String[] categories = {"meme", "hell-joke", "normal", "spam"};

        // Then
        for (String category : categories) {
            int priority = tool.getCategoryPriority(category);
            assertTrue(priority > 0, "Category " + category + " should have positive priority");
        }
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

    @Test
    @DisplayName("空字符串分类应该返回无效")
    void emptyStringShouldBeInvalid() {
        assertFalse(tool.isValidCategory(""));
        assertFalse(tool.isValidCategory("   "));
    }

    @Test
    @DisplayName("应该正确识别敏感和非敏感分类")
    void shouldCorrectlyIdentifySensitiveCategories() {
        // 敏感分类
        assertTrue(tool.isSensitiveCategory("hell-joke"));
        assertTrue(tool.isSensitiveCategory("regional-black"));
        assertTrue(tool.isSensitiveCategory("political"));
        assertTrue(tool.isSensitiveCategory("nsfw"));

        // 非敏感分类
        assertFalse(tool.isSensitiveCategory("meme"));
        assertFalse(tool.isSensitiveCategory("normal"));
        assertFalse(tool.isSensitiveCategory("spam"));
        assertFalse(tool.isSensitiveCategory("ad"));
        assertFalse(tool.isSensitiveCategory("other"));
    }

    @Test
    @DisplayName("获取分类描述应该不为空")
    void getCategoryDescriptionShouldNotBeEmpty() {
        // Given
        String[] categories = {"meme", "hell-joke", "normal", "spam", "ad"};

        // Then
        for (String category : categories) {
            String description = tool.getCategoryDescription(category);
            assertNotNull(description);
            assertFalse(description.isBlank());
            assertNotEquals("Unknown category", description);
        }
    }

    @Test
    @DisplayName("无效分类的描述应该返回Unknown")
    void invalidCategoryShouldReturnUnknownDescription() {
        // Given
        String invalidCode = "invalid";

        // When
        String description = tool.getCategoryDescription(invalidCode);

        // Then
        assertEquals("Unknown category", description);
    }

    @Test
    @DisplayName("分类列表应该包含所有默认分类")
    void categoryListShouldContainAllDefaults() {
        // When
        String result = tool.listCategories();

        // Then
        assertNotNull(result);
        assertTrue(result.contains("meme"));
        assertTrue(result.contains("hell-joke"));
        assertTrue(result.contains("normal"));
    }

    @Test
    @DisplayName("分类详情应该包含必要字段")
    void categoryDetailsShouldContainRequiredFields() {
        // Given
        String categoryCode = "meme";

        // When
        String result = tool.getCategoryDetails(categoryCode);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("code") || result.contains("meme"));
        assertTrue(result.contains("name") || result.contains("弔图"));
    }
}
