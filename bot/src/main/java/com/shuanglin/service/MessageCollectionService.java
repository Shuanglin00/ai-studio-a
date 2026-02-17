package com.shuanglin.service;

import com.shuanglin.dao.classification.CollectedMessage;
import com.shuanglin.dao.classification.MessageCollectionBatch;
import com.shuanglin.dao.classification.enums.BatchStatus;
import com.shuanglin.dao.classification.repository.MessageCollectionBatchRepository;
import com.shuanglin.framework.milky.client.MilkyApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * 消息收集服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageCollectionService {

    private final MessageCollectionBatchRepository batchRepository;
    private final MilkyApiClient milkyApiClient;

    /**
     * 启动消息收集
     */
    public String startCollection(String groupId, Long triggerMessageId, String triggerMessageType, String triggerContent) {
        log.info("Starting message collection for group {}, trigger message: {}", groupId, triggerMessageId);

        MessageCollectionBatch batch = MessageCollectionBatch.builder()
                .id(UUID.randomUUID().toString())
                .groupId(groupId)
                .triggerMessageId(triggerMessageId)
                .triggerMessageType(triggerMessageType)
                .triggerContent(triggerContent)
                .status(BatchStatus.COLLECTING)
                .messageCount(0)
                .messages(new ArrayList<>())
                .collectStartTime(LocalDateTime.now())
                .build();

        if (batchRepository != null) {
            batchRepository.save(batch);
        }

        // 异步收集消息
        collectMessagesAsync(batch);

        return batch.getId();
    }

    /**
     * 异步收集消息
     */
    @Async
    protected CompletableFuture<Void> collectMessagesAsync(MessageCollectionBatch batch) {
        try {
            // 等待一段时间让消息产生
            Thread.sleep(5000);

            // 这里应该调用OneBot API获取后续消息
            // 简化版本：生成一些mock消息用于测试
            List<CollectedMessage> messages = generateMockMessages(10);

            batch.setMessages(messages);
            batch.setMessageCount(messages.size());
            batch.setStatus(BatchStatus.COMPLETED);
            batch.setCollectEndTime(LocalDateTime.now());

            if (batchRepository != null) {
                batchRepository.save(batch);
            }

            log.info("Message collection completed for batch: {}, collected {} messages",
                    batch.getId(), messages.size());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Message collection interrupted", e);
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * 获取批次
     */
    public Optional<MessageCollectionBatch> getBatch(String batchId) {
        if (batchRepository == null) {
            return Optional.empty();
        }
        return batchRepository.findById(batchId);
    }

    /**
     * 更新分类结果
     */
    public void updateClassification(String batchId, String categoryCode, Long userId) {
        // 实际实现中这里会更新分类结果
        log.info("Updating classification for batch {}: category={}, user={}",
                batchId, categoryCode, userId);
    }

    /**
     * 生成Mock消息（用于测试）
     */
    private List<CollectedMessage> generateMockMessages(int count) {
        List<CollectedMessage> messages = new ArrayList<>();
        long baseTimestamp = System.currentTimeMillis();

        String[] sampleMessages = {
            "哈哈哈哈", "笑死我了", "这图太搞了",
            "确实", "说得好", "111", "666"
        };

        for (int i = 0; i < count; i++) {
            CollectedMessage message = CollectedMessage.builder()
                    .messageId(baseTimestamp + i)
                    .userId(10000L + (long) (Math.random() * 90000))
                    .nickname("用户" + i)
                    .content(sampleMessages[i % sampleMessages.length])
                    .timestamp(baseTimestamp + (i * 1000L))
                    .isReply(false)
                    .build();
            messages.add(message);
        }

        return messages;
    }
}
