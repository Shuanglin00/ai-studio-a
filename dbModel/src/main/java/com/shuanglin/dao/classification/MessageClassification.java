package com.shuanglin.dao.classification;

import com.shuanglin.dao.classification.enums.ClassificationSource;
import com.shuanglin.dao.classification.enums.MessageCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息分类结果实体
 */
@Document("message_classification")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageClassification {

    @Id
    private String id;

    /**
     * 关联批次ID
     */
    private String batchId;

    /**
     * 群号
     */
    private String groupId;

    /**
     * 触发消息ID
     */
    private Long triggerMessageId;

    /**
     * 主分类
     */
    private MessageCategory category;

    /**
     * 子分类标签
     */
    private List<MessageCategory> subCategories;

    /**
     * 置信度 0-1
     */
    private Double confidence;

    /**
     * 分类理由
     */
    private String reasoning;

    /**
     * 来源：AUTO/MANUAL
     */
    private ClassificationSource source;

    /**
     * 分类者（手动标注时）
     */
    private Long classifiedBy;

    /**
     * 分类时间
     */
    private LocalDateTime classifiedAt;
}
