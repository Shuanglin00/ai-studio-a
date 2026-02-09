package com.shuanglin.framework.milky.model.request;
import lombok.AllArgsConstructor;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 撤回群聊消息请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecallGroupMessageRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 群ID
     */
    private Long groupId;

    /**
     * 消息序列号
     */
    private Long messageSeq;
}
