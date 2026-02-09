package com.shuanglin.framework.milky.model.request;
import lombok.AllArgsConstructor;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 获取群文件列表请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetGroupFilesRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 群ID
     */
    private Long groupId;

    /**
     * 目录ID
     */
    private String folderId;

    /**
     * 文件数量上限
     */
    private Integer pageStart;

    /**
     * 文件偏移量
     */
    private Integer pageNum;

    /**
     * 排序依据
     */
    private String sortBy;

    /**
     * 排序方式
     */
    private String order;
}
