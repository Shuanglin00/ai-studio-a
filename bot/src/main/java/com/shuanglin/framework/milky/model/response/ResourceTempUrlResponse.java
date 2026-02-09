package com.shuanglin.framework.milky.model.response;

import lombok.Data;

import java.io.Serializable;

/**
 * 临时资源链接响应
 */
@Data
public class ResourceTempUrlResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 资源临时链接
     */
    private String url;
}
