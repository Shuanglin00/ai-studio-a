package com.shuanglin.dao.classification;

import com.shuanglin.dao.classification.enums.BatchStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息收集批次实体
 */
@Document("message_collection_batch")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageCollectionBatch {

    @Id
    private String id;

    /**
     * 群号
     */
    private String groupId;

    /**
     * 触发消息ID（机器人发送的消息）
     */
    private Long triggerMessageId;

    /**
     * 触发消息类型：IMAGE/FORWARD
     */
    private String triggerMessageType;

    /**
     * 触发消息内容摘要
     */
    private String triggerContent;

    /**
     * 收集的消息列表
     */
    private List<CollectedMessage> messages;

    /**
     * 实际收集数量
     */
    private Integer messageCount;

    /**
     * 收集开始时间
     */
    private LocalDateTime collectStartTime;

    /**
     * 收集结束时间
     */
    private LocalDateTime collectEndTime;

    /**
     * 批次状态
     */
    private BatchStatus status;
}
