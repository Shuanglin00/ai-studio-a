package com.shuanglin.framework.milky.model.request;
import lombok.AllArgsConstructor;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 获取陌生人信息请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetStrangerInfoRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 是否不使用缓存
     */
    private Boolean noCache;
}
