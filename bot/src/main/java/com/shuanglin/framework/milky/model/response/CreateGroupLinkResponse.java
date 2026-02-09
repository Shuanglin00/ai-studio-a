package com.shuanglin.framework.milky.model.response;

import lombok.Data;

import java.io.Serializable;

/**
 * 创建群链接响应
 */
@Data
public class CreateGroupLinkResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 链接URL
     */
    private String url;

    /**
     * 链接GC号
     */
    private Long linkG;
}
