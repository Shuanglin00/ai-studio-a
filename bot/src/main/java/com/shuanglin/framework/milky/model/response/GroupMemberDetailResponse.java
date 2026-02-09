package com.shuanglin.framework.milky.model.response;

import lombok.Data;

import java.io.Serializable;

/**
 * 群成员详情响应
 */
@Data
public class GroupMemberDetailResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 群成员信息
     */
    private GroupMemberInfo member;
}
