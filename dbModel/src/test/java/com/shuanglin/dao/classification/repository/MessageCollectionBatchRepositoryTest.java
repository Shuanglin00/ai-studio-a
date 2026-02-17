package com.shuanglin.dao.classification.repository;

import com.shuanglin.dao.classification.CollectedMessage;
import com.shuanglin.dao.classification.MessageCollectionBatch;
import com.shuanglin.dao.classification.enums.BatchStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 消息收集批次Repository集成测试
 */
@DataMongoTest
@DisplayName("MessageCollectionBatchRepository集成测试")
class MessageCollectionBatchRepositoryTest {

    @Autowired
    private MessageCollectionBatchRepository repository;

    @Test
    @DisplayName("应该能保存和查询批次")
    void shouldSaveAndFindBatch() {
        // Given
        MessageCollectionBatch batch = createTestBatch();

        // When
        MessageCollectionBatch saved = repository.save(batch);
        Optional<MessageCollectionBatch> found = repository.findById(saved.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals(batch.getGroupId(), found.get().getGroupId());
        assertEquals(batch.getTriggerMessageId(), found.get().getTriggerMessageId());
    }

    @Test
    @DisplayName("应该能根据群号查询批次")
    void shouldFindByGroupId() {
        // Given
        String groupId = "123456789";
        MessageCollectionBatch batch1 = createBatchWithGroupId(groupId);
        MessageCollectionBatch batch2 = createBatchWithGroupId(groupId);
        MessageCollectionBatch batch3 = createBatchWithGroupId("987654321");

        repository.saveAll(Arrays.asList(batch1, batch2, batch3));

        // When
        List<MessageCollectionBatch> results = repository.findByGroupId(groupId);

        // Then
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(b -> b.getGroupId().equals(groupId)));
    }

    @Test
    @DisplayName("应该能根据状态查询批次")
    void shouldFindByStatus() {
        // Given
        MessageCollectionBatch collecting = createBatchWithStatus(BatchStatus.COLLECTING);
        MessageCollectionBatch completed = createBatchWithStatus(BatchStatus.COMPLETED);

        repository.saveAll(Arrays.asList(collecting, completed));

        // When
        List<MessageCollectionBatch> results = repository.findByStatus(BatchStatus.COLLECTING);

        // Then
        assertEquals(1, results.size());
        assertEquals(BatchStatus.COLLECTING, results.get(0).getStatus());
    }

    @Test
    @DisplayName("应该能根据群号和状态查询批次")
    void shouldFindByGroupIdAndStatus() {
        // Given
        String groupId = "123456789";
        MessageCollectionBatch batch = MessageCollectionBatch.builder()
                .id(UUID.randomUUID().toString())
                .groupId(groupId)
                .status(BatchStatus.COMPLETED)
                .build();

        repository.save(batch);

        // When
        List<MessageCollectionBatch> results = repository.findByGroupIdAndStatus(groupId, BatchStatus.COMPLETED);

        // Then
        assertEquals(1, results.size());
        assertEquals(groupId, results.get(0).getGroupId());
        assertEquals(BatchStatus.COMPLETED, results.get(0).getStatus());
    }

    @Test
    @DisplayName("应该能根据触发消息ID查询批次")
    void shouldFindByTriggerMessageId() {
        // Given
        Long triggerMessageId = 12345L;
        MessageCollectionBatch batch = MessageCollectionBatch.builder()
                .id(UUID.randomUUID().toString())
                .groupId("123456789")
                .triggerMessageId(triggerMessageId)
                .build();

        repository.save(batch);

        // When
        Optional<MessageCollectionBatch> result = repository.findByTriggerMessageId(triggerMessageId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(triggerMessageId, result.get().getTriggerMessageId());
    }

    @Test
    @DisplayName("应该能根据群号和触发消息ID查询批次")
    void shouldFindByGroupIdAndTriggerMessageId() {
        // Given
        String groupId = "123456789";
        Long triggerMessageId = 12345L;
        MessageCollectionBatch batch = MessageCollectionBatch.builder()
                .id(UUID.randomUUID().toString())
                .groupId(groupId)
                .triggerMessageId(triggerMessageId)
                .build();

        repository.save(batch);

        // When
        Optional<MessageCollectionBatch> result = repository.findByGroupIdAndTriggerMessageId(groupId, triggerMessageId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(groupId, result.get().getGroupId());
        assertEquals(triggerMessageId, result.get().getTriggerMessageId());
    }

    @Test
    @DisplayName("应该能根据时间范围查询批次")
    void shouldFindByTimeRange() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        MessageCollectionBatch batch = MessageCollectionBatch.builder()
                .id(UUID.randomUUID().toString())
                .groupId("123456789")
                .collectStartTime(now.minusHours(1))
                .build();

        repository.save(batch);

        // When
        List<MessageCollectionBatch> results = repository.findByCollectStartTimeBetween(now.minusHours(2), now);

        // Then
        assertFalse(results.isEmpty());
    }

    @Test
    @DisplayName("应该能统计状态数量")
    void shouldCountByStatus() {
        // Given
        MessageCollectionBatch batch1 = createBatchWithStatus(BatchStatus.COMPLETED);
        MessageCollectionBatch batch2 = createBatchWithStatus(BatchStatus.COMPLETED);
        MessageCollectionBatch batch3 = createBatchWithStatus(BatchStatus.COLLECTING);

        repository.saveAll(Arrays.asList(batch1, batch2, batch3));

        // When
        long count = repository.countByStatus(BatchStatus.COMPLETED);

        // Then
        assertTrue(count >= 2);
    }

    @Test
    @DisplayName("应该能删除指定群号的所有批次")
    void shouldDeleteByGroupId() {
        // Given
        String groupId = "DELETE_TEST_GROUP";
        MessageCollectionBatch batch = createBatchWithGroupId(groupId);
        repository.save(batch);

        // When
        repository.deleteByGroupId(groupId);
        List<MessageCollectionBatch> results = repository.findByGroupId(groupId);

        // Then
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("保存的批次应该包含嵌套消息")
    void shouldSaveBatchWithMessages() {
        // Given
        List<CollectedMessage> messages = Arrays.asList(
                createMessage(1L, "消息1"),
                createMessage(2L, "消息2")
        );

        MessageCollectionBatch batch = MessageCollectionBatch.builder()
                .id(UUID.randomUUID().toString())
                .groupId("123456789")
                .messages(messages)
                .messageCount(messages.size())
                .build();

        // When
        MessageCollectionBatch saved = repository.save(batch);
        Optional<MessageCollectionBatch> found = repository.findById(saved.getId());

        // Then
        assertTrue(found.isPresent());
        assertNotNull(found.get().getMessages());
        assertEquals(2, found.get().getMessages().size());
    }

    private MessageCollectionBatch createTestBatch() {
        return MessageCollectionBatch.builder()
                .id(UUID.randomUUID().toString())
                .groupId("123456789")
                .triggerMessageId(11111L)
                .triggerMessageType("IMAGE")
                .triggerContent("Test content")
                .status(BatchStatus.COLLECTING)
                .messageCount(0)
                .collectStartTime(LocalDateTime.now())
                .build();
    }

    private MessageCollectionBatch createBatchWithGroupId(String groupId) {
        return MessageCollectionBatch.builder()
                .id(UUID.randomUUID().toString())
                .groupId(groupId)
                .triggerMessageId(System.currentTimeMillis())
                .status(BatchStatus.COLLECTING)
                .build();
    }

    private MessageCollectionBatch createBatchWithStatus(BatchStatus status) {
        return MessageCollectionBatch.builder()
                .id(UUID.randomUUID().toString())
                .groupId("123456789")
                .triggerMessageId(System.currentTimeMillis())
                .status(status)
                .build();
    }

    private CollectedMessage createMessage(Long id, String content) {
        return CollectedMessage.builder()
                .messageId(id)
                .userId(12345L)
                .nickname("测试用户")
                .content(content)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
