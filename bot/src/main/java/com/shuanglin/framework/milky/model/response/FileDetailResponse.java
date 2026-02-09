package com.shuanglin.framework.milky.model.response;

import lombok.Data;

import java.io.Serializable;

/**
 * 文件详情响应
 */
@Data
public class FileDetailResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 文件信息
     */
    private FileInfo file;
}
