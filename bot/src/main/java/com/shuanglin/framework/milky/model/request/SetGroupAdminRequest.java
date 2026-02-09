package com.shuanglin.framework.milky.model.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;

/**
 * 设置群管理员请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SetGroupAdminRequest implements Serializable {
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
     * 是否设置为管理员
     */
    private Boolean enable;
}
