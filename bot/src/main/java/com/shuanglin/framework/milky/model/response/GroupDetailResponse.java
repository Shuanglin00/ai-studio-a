package com.shuanglin.framework.milky.model.response;

import lombok.Data;

import java.io.Serializable;

/**
 * 群详情响应
 */
@Data
public class GroupDetailResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 群信息
     */
    private GroupInfo group;
}
