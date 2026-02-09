package com.shuanglin.framework.milky.model.response;

import lombok.Data;

import java.io.Serializable;

/**
 * 群整体信息响应
 */
@Data
public class GroupWholeResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 群整体信息
     */
    private GroupWholeInfo groupWholeInfo;
}
