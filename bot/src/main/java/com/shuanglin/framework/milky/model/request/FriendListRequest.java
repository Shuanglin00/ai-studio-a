package com.shuanglin.framework.milky.model.request;
import lombok.AllArgsConstructor;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 获取好友列表请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FriendListRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 是否不使用缓存
     */
    private Boolean noCache;
}
