package com.shuanglin.dao.classification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 收集的消息项（嵌套文档）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectedMessage {

    /**
     * 消息ID
     */
    private Long messageId;

    /**
     * 发送者QQ
     */
    private Long userId;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 消息内容（文本）
     */
    private String content;

    /**
     * 消息段（图片、表情等）
     */
    private List<MessageSegment> segments;

    /**
     * 发送时间戳
     */
    private Long timestamp;

    /**
     * 是否是回复消息
     */
    private Boolean isReply;

    /**
     * 回复的消息ID
     */
    private Long replyToMessageId;
}
