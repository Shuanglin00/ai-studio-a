package com.shuanglin.dao.classification.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 批次状态枚举测试
 */
@DisplayName("批次状态枚举测试")
class BatchStatusTest {

    @Test
    @DisplayName("应该包含所有预定义状态")
    void shouldContainAllPredefinedStatuses() {
        BatchStatus[] statuses = BatchStatus.values();
        assertEquals(4, statuses.length);
        assertTrue(containsStatus(statuses, "COLLECTING"));
        assertTrue(containsStatus(statuses, "COMPLETED"));
        assertTrue(containsStatus(statuses, "CLASSIFIED"));
        assertTrue(containsStatus(statuses, "FAILED"));
    }

    @ParameterizedTest
    @EnumSource(BatchStatus.class)
    @DisplayName("每个状态应该有非空的编码和描述")
    void eachStatusShouldHaveNonNullCodeAndDescription(BatchStatus status) {
        assertNotNull(status.getCode());
        assertNotNull(status.getDescription());
        assertFalse(status.getCode().trim().isEmpty());
        assertFalse(status.getDescription().trim().isEmpty());
    }

    @ParameterizedTest
    @CsvSource({
        "COLLECTING, collecting, 收集中",
        "COMPLETED, completed, 收集完成",
        "CLASSIFIED, classified, 已分类",
        "FAILED, failed, 处理失败"
    })
    @DisplayName("应该正确返回状态编码和描述")
    void shouldReturnCorrectCodeAndDescription(String enumName, String expectedCode, String expectedDesc) {
        BatchStatus status = BatchStatus.valueOf(enumName);
        assertEquals(expectedCode, status.getCode());
        assertEquals(expectedDesc, status.getDescription());
    }

    @ParameterizedTest
    @CsvSource({
        "collecting, COLLECTING",
        "completed, COMPLETED",
        "classified, CLASSIFIED",
        "failed, FAILED"
    })
    @DisplayName("应该能通过编码查找状态")
    void shouldFindStatusByCode(String code, String expectedEnumName) {
        BatchStatus result = BatchStatus.fromCode(code);
        assertEquals(BatchStatus.valueOf(expectedEnumName), result);
    }

    @Test
    @DisplayName("查找不存在的编码应该抛出异常")
    void shouldThrowExceptionForInvalidCode() {
        assertThrows(IllegalArgumentException.class, () -> {
            BatchStatus.fromCode("invalid");
        });
    }

    @ParameterizedTest
    @EnumSource(value = BatchStatus.class, names = {"COLLECTING"})
    @DisplayName("COLLECTING应该是进行中状态")
    void collectingShouldBeInProgress(BatchStatus status) {
        assertTrue(status.isInProgress());
        assertFalse(status.isTerminal());
    }

    @ParameterizedTest
    @EnumSource(value = BatchStatus.class, names = {"COMPLETED", "CLASSIFIED", "FAILED"})
    @DisplayName("COMPLETED/CLASSIFIED/FAILED应该是终止状态")
    void completedShouldBeTerminal(BatchStatus status) {
        assertFalse(status.isInProgress());
        assertTrue(status.isTerminal());
    }

    private boolean containsStatus(BatchStatus[] statuses, String name) {
        for (BatchStatus status : statuses) {
            if (status.name().equals(name)) {
                return true;
            }
        }
        return false;
    }
}
