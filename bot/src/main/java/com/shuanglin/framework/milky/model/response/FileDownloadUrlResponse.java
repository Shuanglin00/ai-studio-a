package com.shuanglin.framework.milky.model.response;

import lombok.Data;

import java.io.Serializable;

/**
 * 文件下载地址响应
 */
@Data
public class FileDownloadUrlResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 下载URL
     */
    private String url;
}
