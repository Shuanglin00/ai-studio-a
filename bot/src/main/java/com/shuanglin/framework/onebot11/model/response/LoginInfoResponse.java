package com.shuanglin.framework.onebot11.model.response;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * 登录信息响应
 */
@Data
public class LoginInfoResponse {

    /**
     * QQ 号
     */
    @SerializedName("user_id")
    private Long userId;

    /**
     * QQ 昵称
     */
    private String nickname;
}
