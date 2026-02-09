package com.shuanglin.framework.milky.model.response;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 历史消息列表响应
 */
@Data
public class HistoryMessagesResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 消息列表
     */
    private List<MessageContent> messages;

    /**
     * 下一条消息的序列号
     */
    private Long nextMessageSeq;
}
