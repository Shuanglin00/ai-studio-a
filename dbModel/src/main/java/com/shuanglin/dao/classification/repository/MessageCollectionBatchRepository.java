package com.shuanglin.dao.classification.repository;

import com.shuanglin.dao.classification.MessageCollectionBatch;
import com.shuanglin.dao.classification.enums.BatchStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 消息收集批次Repository
 */
@Repository
public interface MessageCollectionBatchRepository extends MongoRepository<MessageCollectionBatch, String> {

    /**
     * 根据群号查询批次
     */
    List<MessageCollectionBatch> findByGroupId(String groupId);

    /**
     * 根据状态查询批次
     */
    List<MessageCollectionBatch> findByStatus(BatchStatus status);

    /**
     * 根据群号和状态查询批次
     */
    List<MessageCollectionBatch> findByGroupIdAndStatus(String groupId, BatchStatus status);

    /**
     * 根据触发消息ID查询批次
     */
    Optional<MessageCollectionBatch> findByTriggerMessageId(Long triggerMessageId);

    /**
     * 根据群号和触发消息ID查询批次
     */
    Optional<MessageCollectionBatch> findByGroupIdAndTriggerMessageId(String groupId, Long triggerMessageId);

    /**
     * 查询某个时间之后创建的批次
     */
    List<MessageCollectionBatch> findByCollectStartTimeAfter(LocalDateTime time);

    /**
     * 查询某个时间范围内的批次
     */
    List<MessageCollectionBatch> findByCollectStartTimeBetween(LocalDateTime start, LocalDateTime end);

    /**
     * 根据状态计数
     */
    long countByStatus(BatchStatus status);

    /**
     * 根据群号删除批次
     */
    void deleteByGroupId(String groupId);
}
