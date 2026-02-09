package com.shuanglin.framework.milky.model.request;
import lombok.AllArgsConstructor;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 发送好友戳一戳请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendFriendNudgeRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 是否自己戳自己
     */
    private Boolean isSelf;
}
