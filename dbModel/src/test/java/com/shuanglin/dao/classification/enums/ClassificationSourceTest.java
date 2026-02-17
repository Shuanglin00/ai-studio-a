package com.shuanglin.dao.classification.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 分类来源枚举测试
 */
@DisplayName("分类来源枚举测试")
class ClassificationSourceTest {

    @Test
    @DisplayName("应该包含AUTO和MANUAL两种来源")
    void shouldContainAllSources() {
        ClassificationSource[] sources = ClassificationSource.values();
        assertEquals(2, sources.length);
        assertTrue(containsSource(sources, "AUTO"));
        assertTrue(containsSource(sources, "MANUAL"));
    }

    @ParameterizedTest
    @EnumSource(ClassificationSource.class)
    @DisplayName("每个来源应该有非空的编码和描述")
    void eachSourceShouldHaveNonNullCodeAndDescription(ClassificationSource source) {
        assertNotNull(source.getCode());
        assertNotNull(source.getDescription());
        assertFalse(source.getCode().trim().isEmpty());
        assertFalse(source.getDescription().trim().isEmpty());
    }

    @ParameterizedTest
    @CsvSource({
        "AUTO, auto, 自动分类",
        "MANUAL, manual, 手动标注"
    })
    @DisplayName("应该正确返回来源编码和描述")
    void shouldReturnCorrectCodeAndDescription(String enumName, String expectedCode, String expectedDesc) {
        ClassificationSource source = ClassificationSource.valueOf(enumName);
        assertEquals(expectedCode, source.getCode());
        assertEquals(expectedDesc, source.getDescription());
    }

    @ParameterizedTest
    @CsvSource({
        "auto, AUTO",
        "manual, MANUAL"
    })
    @DisplayName("应该能通过编码查找来源")
    void shouldFindSourceByCode(String code, String expectedEnumName) {
        ClassificationSource result = ClassificationSource.fromCode(code);
        assertEquals(ClassificationSource.valueOf(expectedEnumName), result);
    }

    @Test
    @DisplayName("AUTO应该识别为自动分类")
    void autoShouldBeIdentifiedAsAuto() {
        assertTrue(ClassificationSource.AUTO.isAuto());
        assertFalse(ClassificationSource.AUTO.isManual());
    }

    @Test
    @DisplayName("MANUAL应该识别为手动标注")
    void manualShouldBeIdentifiedAsManual() {
        assertTrue(ClassificationSource.MANUAL.isManual());
        assertFalse(ClassificationSource.MANUAL.isAuto());
    }

    private boolean containsSource(ClassificationSource[] sources, String name) {
        for (ClassificationSource source : sources) {
            if (source.name().equals(name)) {
                return true;
            }
        }
        return false;
    }
}
