package com.shuanglin.framework.onebot11.model.response;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * 群成员信息响应
 */
@Data
public class GroupMemberInfoResponse {

    /**
     * 群号
     */
    @SerializedName("group_id")
    private Long groupId;

    /**
     * QQ 号
     */
    @SerializedName("user_id")
    private Long userId;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 群名片
     */
    private String card;

    /**
     * 性别
     */
    private String sex;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 地区
     */
    private String area;

    /**
     * 加群时间戳
     */
    @SerializedName("join_time")
    private Long joinTime;

    /**
     * 最后发言时间戳
     */
    @SerializedName("last_sent_time")
    private Long lastSentTime;

    /**
     * 等级
     */
    private String level;

    /**
     * 角色：owner, admin, member
     */
    private String role;

    /**
     * 专属头衔
     */
    private String title;

    /**
     * 专属头衔过期时间戳
     */
    @SerializedName("title_expire_time")
    private Long titleExpireTime;

    /**
     * 是否不良记录成员
     */
    private Boolean unfriendly;

    /**
     * 禁言到期时间
     */
    @SerializedName("shut_up_timestamp")
    private Long shutUpTimestamp;
}
