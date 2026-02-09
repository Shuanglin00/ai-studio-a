package com.shuanglin.framework.milky.model.response;

import com.shuanglin.framework.milky.enums.Sex;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户个人信息响应
 */
@Data
public class UserProfileResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * QQ号
     */
    private String qid;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 性别
     */
    private Sex sex;

    /**
     * 备注
     */
    private String remark;

    /**
     * 个人说明
     */
    private String bio;

    /**
     * 等级
     */
    private Integer level;

    /**
     * 国家
     */
    private String country;

    /**
     * 城市
     */
    private String city;

    /**
     * 学校
     */
    private String school;
}
