package com.shuanglin.framework.milky.model.response;

import lombok.Data;

import java.io.Serializable;

/**
 * 消息详情响应
 */
@Data
public class MessageDetailResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 消息内容
     */
    private MessageContent message;
}
