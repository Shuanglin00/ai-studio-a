package com.shuanglin.framework.milky.model.response;

import com.shuanglin.framework.milky.enums.Sex;
import lombok.Data;

import java.io.Serializable;

/**
 * 好友信息
 */
@Data
public class FriendInfo implements Serializable {
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
     * QQ号
     */
    private String qid;

    /**
     * 备注
     */
    private String remark;

    /**
     * 分组
     */
    private String category;
}
