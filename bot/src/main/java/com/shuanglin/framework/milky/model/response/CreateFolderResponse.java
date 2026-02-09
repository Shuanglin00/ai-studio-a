package com.shuanglin.framework.milky.model.response;

import lombok.Data;

import java.io.Serializable;

/**
 * 创建群文件目录响应
 */
@Data
public class CreateFolderResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 目录ID
     */
    private String folderId;
}
