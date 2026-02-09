package com.shuanglin.framework.milky.model.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;

/**
 * 设置群专属头衔请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SetGroupSpecialTitleRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 群ID
     */
    private Long groupId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 新专属头衔
     */
    private String newSpecialTitle;

    /**
     * 过期时间
     */
    private Long duration;
}
