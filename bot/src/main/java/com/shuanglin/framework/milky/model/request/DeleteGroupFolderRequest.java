package com.shuanglin.framework.milky.model.request;
import lombok.AllArgsConstructor;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 删除群文件目录请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeleteGroupFolderRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 群ID
     */
    private Long groupId;

    /**
     * 目录ID
     */
    private String folderId;
}
