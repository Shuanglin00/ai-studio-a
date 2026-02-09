package com.shuanglin.framework.milky.model.request;
import lombok.AllArgsConstructor;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 创建群链接请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateGroupLinkRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 群ID
     */
    private Long groupId;

    /**
     * 链接类型
     */
    private Integer type;

    /**
     * 有效期
     */
    private Long expiration;
}
