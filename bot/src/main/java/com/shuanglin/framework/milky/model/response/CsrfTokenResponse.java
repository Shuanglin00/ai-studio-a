package com.shuanglin.framework.milky.model.response;

import lombok.Data;

import java.io.Serializable;

/**
 * CSRF Token响应
 */
@Data
public class CsrfTokenResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * CSRF Token
     */
    private String csrfToken;
}
