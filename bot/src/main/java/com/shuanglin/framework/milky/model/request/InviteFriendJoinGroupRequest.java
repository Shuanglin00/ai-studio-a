package com.shuanglin.framework.milky.model.request;
import lombok.AllArgsConstructor;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 邀请好友加群请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InviteFriendJoinGroupRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 群ID
     */
    private Long groupId;

    /**
     * 用户ID
     */
    private Long[] userIds;

    /**
     * 是否同意加群
     */
    private Boolean invitee;
}
