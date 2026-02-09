package com.shuanglin.framework.milky.model.request;
import lombok.AllArgsConstructor;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 上传群图片请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadGroupImageRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 群ID
     */
    private Long groupId;

    /**
     * 图片类型
     */
    private String type;

    /**
     * 图片文件路径
     */
    private String path;
}
