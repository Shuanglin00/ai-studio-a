package com.shuanglin.framework.milky.model.response;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 好友列表响应
 */
@Data
public class FriendListResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 好友列表
     */
    private List<FriendInfo> friends;
}
