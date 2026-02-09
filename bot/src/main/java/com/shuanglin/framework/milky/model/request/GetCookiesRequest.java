package com.shuanglin.framework.milky.model.request;
import lombok.AllArgsConstructor;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 获取Cookies请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetCookiesRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 域名
     */
    private String domain;
}
