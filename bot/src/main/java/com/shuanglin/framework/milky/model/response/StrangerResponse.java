package com.shuanglin.framework.milky.model.response;

import lombok.Data;

import java.io.Serializable;

/**
 * 陌生人信息响应
 */
@Data
public class StrangerResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 陌生人信息
     */
    private StrangerInfo stranger;
}
