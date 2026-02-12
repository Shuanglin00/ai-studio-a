package com.shuanglin.framework.onebot11.model.response;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * CSRF Token 响应
 */
@Data
public class CsrfTokenResponse {

    /**
     * CSRF Token
     */
    private String token;

    /**
     * CSRF Token（备用字段）
     */
    @SerializedName("csrf_token")
    private Integer csrfToken;
}
