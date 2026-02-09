package com.shuanglin.framework.milky.model.response;

import lombok.Data;

import java.io.Serializable;

/**
 * 群荣誉响应
 */
@Data
public class GroupHonorResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 群荣誉信息
     */
    private GroupHonorInfo groupHonor;
}
