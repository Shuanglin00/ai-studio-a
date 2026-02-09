package com.shuanglin.framework.milky.model.response;

import lombok.Data;

import java.io.Serializable;

/**
 * 消息发送响应
 */
@Data
public class MessageResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 消息序列号
     */
    private Long messageSeq;

    /**
     * 发送时间
     */
    private Long time;
}
