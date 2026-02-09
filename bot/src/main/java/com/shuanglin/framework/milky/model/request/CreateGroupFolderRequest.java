package com.shuanglin.framework.milky.model.request;
import lombok.AllArgsConstructor;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 创建群文件目录请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateGroupFolderRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 群ID
     */
    private Long groupId;

    /**
     * 父目录ID
     */
    private String parentId;

    /**
     * 目录名
     */
    private String name;
}
