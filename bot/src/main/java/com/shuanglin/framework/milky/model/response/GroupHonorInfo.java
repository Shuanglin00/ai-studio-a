package com.shuanglin.framework.milky.model.response;

import lombok.Data;

import java.io.Serializable;

/**
 * 群荣誉信息
 */
@Data
public class GroupHonorInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 群ID
     */
    private Long groupId;

    /**
     * 当前QQ荣誉类型
     */
    private String honorType;

    /**
     * 荣誉信息
     */
    private String[] honor;

    /**
     * 是否存在该荣誉
     */
    private Boolean hasHonor;
}
