package com.shuanglin.framework.milky.model.request;
import lombok.AllArgsConstructor;

import com.shuanglin.framework.milky.enums.MessageScene;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 标记消息为已读请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarkMessageAsReadRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 消息场景
     */
    private MessageScene messageScene;

    /**
     * 对方ID
     */
    private Long peerId;

    /**
     * 消息序列号
     */
    private Long messageSeq;
}
