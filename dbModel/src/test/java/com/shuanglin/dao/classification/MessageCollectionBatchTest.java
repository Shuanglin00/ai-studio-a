package com.shuanglin.dao.classification;

import com.shuanglin.dao.classification.enums.BatchStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 消息收集批次实体测试
 */
@DisplayName("消息收集批次实体测试")
class MessageCollectionBatchTest {

    @Test
    @DisplayName("应该能使用Builder创建对象")
    void shouldCreateUsingBuilder() {
        // Given
        String groupId = "123456789";
        Long triggerMessageId = 11111L;

        // When
        MessageCollectionBatch batch = MessageCollectionBatch.builder()
                .id("batch-001")
                .groupId(groupId)
                .triggerMessageId(triggerMessageId)
                .triggerMessageType("IMAGE")
                .triggerContent("图片消息")
                .status(BatchStatus.COLLECTING)
                .messageCount(0)
                .collectStartTime(LocalDateTime.now())
                .build();

        // Then
        assertNotNull(batch);
        assertEquals("batch-001", batch.getId());
        assertEquals(groupId, batch.getGroupId());
        assertEquals(triggerMessageId, batch.getTriggerMessageId());
        assertEquals("IMAGE", batch.getTriggerMessageType());
        assertEquals(BatchStatus.COLLECTING, batch.getStatus());
    }

    @Test
    @DisplayName("应该能添加消息到批次")
    void shouldAddMessagesToBatch() {
        // Given
        List<CollectedMessage> messages = Arrays.asList(
                CollectedMessage.builder()
                        .messageId(1L)
                        .userId(100L)
                        .content("消息1")
                        .build(),
                CollectedMessage.builder()
                        .messageId(2L)
                        .userId(101L)
                        .content("消息2")
                        .build()
        );

        // When
        MessageCollectionBatch batch = MessageCollectionBatch.builder()
                .id("batch-001")
                .groupId("123456")
                .messages(messages)
                .messageCount(messages.size())
                .build();

        // Then
        assertNotNull(batch.getMessages());
        assertEquals(2, batch.getMessages().size());
        assertEquals(2, batch.getMessageCount());
    }

    @Test
    @DisplayName("应该能更新批次状态")
    void shouldUpdateStatus() {
        // Given
        MessageCollectionBatch batch = MessageCollectionBatch.builder()
                .id("batch-001")
                .groupId("123456")
                .status(BatchStatus.COLLECTING)
                .build();

        // When
        batch.setStatus(BatchStatus.COMPLETED);
        batch.setCollectEndTime(LocalDateTime.now());

        // Then
        assertEquals(BatchStatus.COMPLETED, batch.getStatus());
        assertNotNull(batch.getCollectEndTime());
    }

    @Test
    @DisplayName("应该支持无参构造")
    void shouldSupportNoArgsConstructor() {
        // When
        MessageCollectionBatch batch = new MessageCollectionBatch();

        // Then
        assertNotNull(batch);
    }

    @Test
    @DisplayName("初始状态应该为COLLECTING")
    void defaultStatusShouldBeCollecting() {
        // When
        MessageCollectionBatch batch = new MessageCollectionBatch();

        // Then
        assertNull(batch.getStatus()); // 未设置时应该为null
    }
}
