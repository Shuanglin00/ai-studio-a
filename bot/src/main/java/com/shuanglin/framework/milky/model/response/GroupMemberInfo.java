package com.shuanglin.framework.milky.model.response;

import com.shuanglin.framework.milky.enums.GroupRole;
import com.shuanglin.framework.milky.enums.Sex;
import lombok.Data;

import java.io.Serializable;

/**
 * 群成员信息
 */
@Data
public class GroupMemberInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 性别
     */
    private Sex sex;

    /**
     * 群ID
     */
    private Long groupId;

    /**
     * 群名片
     */
    private String card;

    /**
     * 专属头衔
     */
    private String title;

    /**
     * 等级
     */
    private Integer level;

    /**
     * 角色
     */
    private GroupRole role;

    /**
     * 入群时间
     */
    private Long joinTime;

    /**
     * 最后发言时间
     */
    private Long lastSentTime;

    /**
     * 禁言结束时间
     */
    private Long shutUpEndTime;
}
