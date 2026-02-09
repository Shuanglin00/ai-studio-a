package com.shuanglin.framework.milky.model.response;

import lombok.Data;

import java.io.Serializable;

/**
 * 上传群图片响应
 */
@Data
public class UploadGroupImageResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 图片ID
     */
    private String imageId;
}
