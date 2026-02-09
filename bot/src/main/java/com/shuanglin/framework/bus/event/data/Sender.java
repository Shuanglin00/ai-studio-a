package com.shuanglin.framework.bus.event.data;

import com.google.gson.annotations.SerializedName;
import com.shuanglin.framework.enums.onebot.GroupRole;
import com.shuanglin.framework.enums.onebot.Sex;
import lombok.Data;

/**
 * 消息发送者
 */
@Data
public class Sender {
    @SerializedName("user_id")
    private Long userId;        // 发送者 QQ 号

    @SerializedName("nickname")
    private String nickname;    // 昵称

    @SerializedName("card")
    private String card;        // 群名片/备注

    @SerializedName("sex")
    private Sex sex;            // 性别 male/female/unknown

    @SerializedName("age")
    private Integer age;        // 年龄

    @SerializedName("level")
    private String level;       // 群等级

    @SerializedName("role")
    private GroupRole role;     // 群角色 owner/admin/member

    @SerializedName("title")
    private String title;       // 专属头衔

    @SerializedName("group_id")
    private Long groupId;       // 群号（来自群的临时聊天）

    @SerializedName("area")
    private String area;        // 地区
}
