package com.shuanglin.framework.milky.model.request;
import lombok.AllArgsConstructor;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 设置群头像请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SetGroupAvatarRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 群ID
     */
    private Long groupId;

    /**
     * 头像URI
     */
    private String uri;
}
