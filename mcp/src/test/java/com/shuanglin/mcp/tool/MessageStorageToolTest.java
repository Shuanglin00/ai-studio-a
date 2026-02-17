package com.shuanglin.mcp.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.shuanglin.dao.classification.CollectedMessage;
import com.shuanglin.dao.classification.MessageCollectionBatch;
import com.shuanglin.dao.classification.enums.BatchStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 消息存储工具测试
 */
@DisplayName("消息存储工具测试")
class MessageStorageToolTest {

    private MessageStorageTool tool;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        // 使用null repositories进行单元测试，验证参数处理
        tool = new MessageStorageTool(null, null, objectMapper);
    }

    @Test
    @DisplayName("应该能解析有效的批次JSON")
    void shouldParseValidBatchJson() throws Exception {
        // Given
        MessageCollectionBatch batch = createTestBatch();
        String batchJson = objectMapper.writeValueAsString(batch);

        // When - 使用内部方法解析
        MessageCollectionBatch parsed = tool.parseBatch(batchJson);

        // Then
        assertNotNull(parsed);
        assertEquals(batch.getId(), parsed.getId());
        assertEquals(batch.getGroupId(), parsed.getGroupId());
    }

    @Test
    @DisplayName("解析无效JSON时应该抛出异常")
    void shouldThrowExceptionForInvalidJson() {
        // Given
        String invalidJson = "invalid json";

        // Then
        assertThrows(Exception.class, () -> {
            tool.parseBatch(invalidJson);
        });
    }

    @Test
    @DisplayName("应该能验证批次数据的完整性")
    void shouldValidateBatchCompleteness() {
        // Given - 完整的批次
        MessageCollectionBatch validBatch = MessageCollectionBatch.builder()
                .id("batch-001")
                .groupId("123456")
                .triggerMessageId(1L)
                .status(BatchStatus.COMPLETED)
                .messageCount(10)
                .messages(Arrays.asList(
                        CollectedMessage.builder().messageId(1L).content("test").build()
                ))
                .build();

        // When & Then
        assertDoesNotThrow(() -> tool.validateBatch(validBatch));
    }

    @Test
    @DisplayName("验证空批次时应该抛出异常")
    void shouldThrowExceptionForEmptyBatch() {
        // Given
        MessageCollectionBatch emptyBatch = new MessageCollectionBatch();

        // Then
        assertThrows(IllegalArgumentException.class, () -> {
            tool.validateBatch(emptyBatch);
        });
    }

    @Test
    @DisplayName("应该能正确提取批次ID")
    void shouldExtractBatchId() throws Exception {
        // Given
        String batchId = "test-batch-001";
        MessageCollectionBatch batch = MessageCollectionBatch.builder()
                .id(batchId)
                .groupId("123456")
                .build();
        String batchJson = objectMapper.writeValueAsString(batch);

        // When
        String extractedId = tool.extractBatchId(batchJson);

        // Then
        assertEquals(batchId, extractedId);
    }

    @Test
    @DisplayName("应该能正确提取群号")
    void shouldExtractGroupId() throws Exception {
        // Given
        String groupId = "123456789";
        MessageCollectionBatch batch = MessageCollectionBatch.builder()
                .id("batch-001")
                .groupId(groupId)
                .build();
        String batchJson = objectMapper.writeValueAsString(batch);

        // When
        String extractedGroupId = tool.extractGroupId(batchJson);

        // Then
        assertEquals(groupId, extractedGroupId);
    }

    @Test
    @DisplayName("应该能统计消息数量")
    void shouldCountMessages() throws Exception {
        // Given
        List<CollectedMessage> messages = Arrays.asList(
                CollectedMessage.builder().messageId(1L).build(),
                CollectedMessage.builder().messageId(2L).build(),
                CollectedMessage.builder().messageId(3L).build()
        );
        MessageCollectionBatch batch = MessageCollectionBatch.builder()
                .messages(messages)
                .messageCount(messages.size())
                .build();
        String batchJson = objectMapper.writeValueAsString(batch);

        // When
        int count = tool.countMessages(batchJson);

        // Then
        assertEquals(3, count);
    }

    @Test
    @DisplayName("空批次的消息数应该为0")
    void emptyBatchShouldHaveZeroMessages() throws Exception {
        // Given
        MessageCollectionBatch batch = MessageCollectionBatch.builder()
                .id("batch-001")
                .groupId("123456")
                .build();
        String batchJson = objectMapper.writeValueAsString(batch);

        // When
        int count = tool.countMessages(batchJson);

        // Then
        assertEquals(0, count);
    }

    private MessageCollectionBatch createTestBatch() {
        return MessageCollectionBatch.builder()
                .id("test-batch-001")
                .groupId("123456789")
                .triggerMessageId(11111L)
                .triggerMessageType("IMAGE")
                .triggerContent("Test content")
                .status(BatchStatus.COMPLETED)
                .messageCount(5)
                .collectStartTime(LocalDateTime.now().minusMinutes(5))
                .collectEndTime(LocalDateTime.now())
                .build();
    }
}
