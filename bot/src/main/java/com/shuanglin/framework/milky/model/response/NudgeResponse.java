package com.shuanglin.framework.milky.model.response;

import lombok.Data;

import java.io.Serializable;

/**
 * 戳一戳响应
 */
@Data
public class NudgeResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 是否成功
     */
    private Boolean success;
}
