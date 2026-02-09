package com.shuanglin.framework.milky.model.response;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 群列表响应
 */
@Data
public class GroupListResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 群列表
     */
    private List<GroupInfo> groups;
}
