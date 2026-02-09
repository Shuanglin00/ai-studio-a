package com.shuanglin.framework.milky.model.response;

import lombok.Data;

import java.io.Serializable;

/**
 * 群整体信息
 */
@Data
public class GroupWholeInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 群ID
     */
    private Long groupId;

    /**
     * 群名称
     */
    private String groupName;

    /**
     * 群头像URL
     */
    private String groupAvatar;

    /**
     * 群创建时间
     */
    private Long groupCreateTime;

    /**
     * 群等级
     */
    private Integer groupLevel;

    /**
     * 成员数量
     */
    private Integer memberCount;

    /**
     * 最大成员数量
     */
    private Integer maxMemberCount;

    /**
     * 群主ID
     */
    private Long ownerId;

    /**
     * 群主名称
     */
    private String ownerName;

    /**
     * 群介绍
     */
    String groupIntroduction;

    /**
     * 群公告
     */
    String groupAnnouncement;

    /**
     * 群成员列表
     */
    GroupMemberInfo[] members;

    /**
     * 机器人是否在群中
     */
    Boolean botInGroup;
}
