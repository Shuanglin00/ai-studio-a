package com.shuanglin.framework.milky.model.response;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 合并转发消息响应
 */
@Data
public class ForwardedMessagesResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 消息列表
     */
    private List<MessageContent> messages;
}
