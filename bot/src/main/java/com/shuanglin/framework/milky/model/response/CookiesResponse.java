package com.shuanglin.framework.milky.model.response;

import lombok.Data;

import java.io.Serializable;

/**
 * Cookies响应
 */
@Data
public class CookiesResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Cookies字符串
     */
    private String cookies;
}
