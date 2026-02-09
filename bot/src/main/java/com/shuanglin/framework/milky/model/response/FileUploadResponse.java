package com.shuanglin.framework.milky.model.response;

import lombok.Data;

import java.io.Serializable;

/**
 * 文件上传响应
 */
@Data
public class FileUploadResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 文件ID
     */
    private String fileId;

    /**
     * 文件URL
     */
    private String url;
}
