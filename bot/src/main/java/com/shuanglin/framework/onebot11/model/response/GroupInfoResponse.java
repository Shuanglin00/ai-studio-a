package com.shuanglin.framework.onebot11.model.response;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * 群信息响应
 */
@Data
public class GroupInfoResponse {

    /**
     * 群号
     */
    @SerializedName("group_id")
    private Long groupId;

    /**
     * 群名称
     */
    @SerializedName("group_name")
    private String groupName;

    /**
     * 群备注
     */
    @SerializedName("group_memo")
    private String groupMemo;

    /**
     * 群创建时间
     */
    @SerializedName("group_create_time")
    private Long groupCreateTime;

    /**
     * 成员数
     */
    @SerializedName("member_count")
    private Integer memberCount;

    /**
     * 最大成员数
     */
    @SerializedName("max_member_count")
    private Integer maxMemberCount;

    /**
     * 群头像
     */
    @SerializedName("avatar_url")
    private String avatarUrl;

    /**
     * 群主 QQ 号
     */
    @SerializedName("owner_id")
    private Long ownerId;
}
