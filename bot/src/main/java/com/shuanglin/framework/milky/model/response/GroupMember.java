package com.shuanglin.framework.milky.model.response;

import com.shuanglin.framework.milky.enums.Sex;
import lombok.Data;

import java.io.Serializable;

/**
 * 群成员（好友列表中的群成员）
 */
@Data
public class GroupMember implements Serializable {
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
}
