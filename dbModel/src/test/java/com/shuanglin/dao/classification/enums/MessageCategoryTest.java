package com.shuanglin.dao.classification.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 消息分类枚举测试
 */
@DisplayName("消息分类枚举测试")
class MessageCategoryTest {

    @Test
    @DisplayName("应该包含所有预定义分类")
    void shouldContainAllPredefinedCategories() {
        // Given & When
        MessageCategory[] categories = MessageCategory.values();

        // Then
        assertEquals(9, categories.length);
        assertTrue(containsCategory(categories, "MEME"));
        assertTrue(containsCategory(categories, "HELL_JOKE"));
        assertTrue(containsCategory(categories, "REGIONAL_BLACK"));
        assertTrue(containsCategory(categories, "POLITICAL"));
        assertTrue(containsCategory(categories, "NSFW"));
        assertTrue(containsCategory(categories, "NORMAL"));
        assertTrue(containsCategory(categories, "SPAM"));
        assertTrue(containsCategory(categories, "AD"));
        assertTrue(containsCategory(categories, "OTHER"));
    }

    @ParameterizedTest
    @EnumSource(MessageCategory.class)
    @DisplayName("每个分类应该有非空的编码和名称")
    void eachCategoryShouldHaveNonNullCodeAndName(MessageCategory category) {
        assertNotNull(category.getCode());
        assertNotNull(category.getName());
        assertFalse(category.getCode().trim().isEmpty());
        assertFalse(category.getName().trim().isEmpty());
    }

    @ParameterizedTest
    @CsvSource({
        "MEME, 弔图",
        "HELL_JOKE, 地狱笑话",
        "REGIONAL_BLACK, 地域黑",
        "POLITICAL, 政治敏感",
        "NSFW, NSFW",
        "NORMAL, 正常",
        "SPAM, 刷屏",
        "AD, 广告",
        "OTHER, 其他"
    })
    @DisplayName("应该正确返回分类名称")
    void shouldReturnCorrectName(String code, String expectedName) {
        MessageCategory category = MessageCategory.valueOf(code);
        assertEquals(expectedName, category.getName());
    }

    @ParameterizedTest
    @CsvSource({
        "MEME, meme",
        "HELL_JOKE, hell-joke",
        "REGIONAL_BLACK, regional-black",
        "POLITICAL, political",
        "NSFW, nsfw",
        "NORMAL, normal",
        "SPAM, spam",
        "AD, ad",
        "OTHER, other"
    })
    @DisplayName("应该正确返回分类编码")
    void shouldReturnCorrectCode(String enumName, String expectedCode) {
        MessageCategory category = MessageCategory.valueOf(enumName);
        assertEquals(expectedCode, category.getCode());
    }

    @ParameterizedTest
    @CsvSource({
        "meme, MEME",
        "hell-joke, HELL_JOKE",
        "regional-black, REGIONAL_BLACK",
        "political, POLITICAL",
        "nsfw, NSFW",
        "normal, NORMAL",
        "spam, SPAM",
        "ad, AD",
        "other, OTHER"
    })
    @DisplayName("应该能通过编码查找分类")
    void shouldFindCategoryByCode(String code, String expectedEnumName) {
        MessageCategory result = MessageCategory.fromCode(code);
        assertEquals(MessageCategory.valueOf(expectedEnumName), result);
    }

    @Test
    @DisplayName("查找不存在的编码应该抛出异常")
    void shouldThrowExceptionForInvalidCode() {
        assertThrows(IllegalArgumentException.class, () -> {
            MessageCategory.fromCode("invalid-code");
        });
    }

    @Test
    @DisplayName("查找null编码应该抛出异常")
    void shouldThrowExceptionForNullCode() {
        assertThrows(IllegalArgumentException.class, () -> {
            MessageCategory.fromCode(null);
        });
    }

    @ParameterizedTest
    @EnumSource(value = MessageCategory.class, names = {"HELL_JOKE", "REGIONAL_BLACK", "POLITICAL", "NSFW"})
    @DisplayName("敏感分类应该被正确识别")
    void sensitiveCategoriesShouldBeIdentified(MessageCategory category) {
        assertTrue(category.isSensitive());
    }

    @ParameterizedTest
    @EnumSource(value = MessageCategory.class, names = {"MEME", "NORMAL", "SPAM", "AD", "OTHER"})
    @DisplayName("非敏感分类应该被正确识别")
    void nonSensitiveCategoriesShouldBeIdentified(MessageCategory category) {
        assertFalse(category.isSensitive());
    }

    private boolean containsCategory(MessageCategory[] categories, String name) {
        for (MessageCategory category : categories) {
            if (category.name().equals(name)) {
                return true;
            }
        }
        return false;
    }
}
