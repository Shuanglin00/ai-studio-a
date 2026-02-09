package com.shuanglin.framework.milky.model.request;
import lombok.AllArgsConstructor;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 移动群文件请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoveGroupFileRequest implements Serializable {
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
     * 源目录ID
     */
    private String fromFolderId;

    /**
     * 目标目录ID
     */
    private String toFolderId;
}
