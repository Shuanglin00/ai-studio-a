package com.shuanglin.dao.classification.repository;

import com.shuanglin.dao.classification.MessageClassification;
import com.shuanglin.dao.classification.enums.ClassificationSource;
import com.shuanglin.dao.classification.enums.MessageCategory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 消息分类结果Repository
 */
@Repository
public interface MessageClassificationRepository extends MongoRepository<MessageClassification, String> {

    /**
     * 根据批次ID查询分类结果
     */
    Optional<MessageClassification> findByBatchId(String batchId);

    /**
     * 根据群号查询分类结果
     */
    List<MessageClassification> findByGroupId(String groupId);

    /**
     * 根据分类查询
     */
    List<MessageClassification> findByCategory(MessageCategory category);

    /**
     * 根据群号和分类查询
     */
    List<MessageClassification> findByGroupIdAndCategory(String groupId, MessageCategory category);

    /**
     * 根据来源查询
     */
    List<MessageClassification> findBySource(ClassificationSource source);

    /**
     * 根据触发消息ID查询
     */
    Optional<MessageClassification> findByTriggerMessageId(Long triggerMessageId);

    /**
     * 根据分类者查询
     */
    List<MessageClassification> findByClassifiedBy(Long userId);

    /**
     * 查询某个时间范围内的分类
     */
    List<MessageClassification> findByClassifiedAtBetween(LocalDateTime start, LocalDateTime end);

    /**
     * 查询置信度大于指定值的分类
     */
    List<MessageClassification> findByConfidenceGreaterThan(Double confidence);

    /**
     * 根据群号和触发消息ID查询
     */
    Optional<MessageClassification> findByGroupIdAndTriggerMessageId(String groupId, Long triggerMessageId);

    /**
     * 统计某个分类的数量
     */
    long countByCategory(MessageCategory category);

    /**
     * 统计某个群的分类数量
     */
    long countByGroupId(String groupId);
}
