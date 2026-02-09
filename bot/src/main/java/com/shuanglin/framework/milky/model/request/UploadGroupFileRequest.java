package com.shuanglin.framework.milky.model.request;
import lombok.AllArgsConstructor;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 上传群文件请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadGroupFileRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 群ID
     */
    private Long groupId;

    /**
     * 文件相对目录
     */
    private String folderId;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 文件路径
     */
    private String path;
}
