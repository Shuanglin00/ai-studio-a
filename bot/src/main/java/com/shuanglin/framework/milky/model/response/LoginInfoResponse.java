package com.shuanglin.framework.milky.model.response;

import lombok.Data;

import java.io.Serializable;

/**
 * 登录信息响应
 */
@Data
public class LoginInfoResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * QQ账号
     */
    private Long uin;

    /**
     * 昵称
     */
    private String nickname;
}
