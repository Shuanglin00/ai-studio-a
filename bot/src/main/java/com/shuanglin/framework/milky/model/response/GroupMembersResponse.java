package com.shuanglin.framework.milky.model.response;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 群成员列表响应（好友API）
 */
@Data
public class GroupMembersResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 群成员列表
     */
    private List<GroupMember> members;
}
