package com.shuanglin.framework.milky.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 重命名群文件请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RenameGroupFileRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 群ID
     */
    private Long groupId;

    /**
     * 文件ID
     */
    private String fileId;

    /**
     * 目录ID
     */
    private String folderId;

    /**
     * 新文件名
     */
    private String newName;
}
