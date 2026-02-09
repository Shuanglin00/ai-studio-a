package com.shuanglin.framework.milky.model.response;

import lombok.Data;

import java.io.Serializable;

/**
 * 好友详情响应
 */
@Data
public class FriendDetailResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 好友信息
     */
    private FriendInfo friend;
}
