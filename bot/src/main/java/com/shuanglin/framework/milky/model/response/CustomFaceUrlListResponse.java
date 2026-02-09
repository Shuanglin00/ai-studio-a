package com.shuanglin.framework.milky.model.response;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 自定义表情URL列表响应
 */
@Data
public class CustomFaceUrlListResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * URL列表
     */
    private List<String> urls;
}
