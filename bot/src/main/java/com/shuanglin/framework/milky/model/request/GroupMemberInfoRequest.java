package com.shuanglin.framework.milky.model.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;

/**
 * 获取群成员信息请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupMemberInfoRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 群ID
     */
    private Long groupId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 是否不使用缓存
     */
    private Boolean noCache;
}
