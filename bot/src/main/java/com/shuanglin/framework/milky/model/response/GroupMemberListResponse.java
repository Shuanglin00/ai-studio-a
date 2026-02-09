package com.shuanglin.framework.milky.model.response;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 群成员列表响应
 */
@Data
public class GroupMemberListResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 群成员列表
     */
    private List<GroupMemberInfo> members;
}
