package com.shuanglin.framework.milky.model.response;

import com.shuanglin.framework.milky.enums.Sex;
import lombok.Data;

import java.io.Serializable;

/**
 * 陌生人信息
 */
@Data
public class StrangerInfo implements Serializable {
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
     * 年龄
     */
    private Integer age;
}
