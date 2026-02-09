package com.shuanglin.framework.milky.model.response;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 文件列表响应
 */
@Data
public class FileListResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 文件列表
     */
    private List<FileInfo> files;

    /**
     * 群文件数量
     */
    private Integer count;
}
