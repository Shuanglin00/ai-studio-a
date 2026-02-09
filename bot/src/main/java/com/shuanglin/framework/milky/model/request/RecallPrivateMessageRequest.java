package com.shuanglin.framework.milky.model.request;
import lombok.AllArgsConstructor;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 撤回私聊消息请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecallPrivateMessageRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 消息序列号
     */
    private Long messageSeq;
}
