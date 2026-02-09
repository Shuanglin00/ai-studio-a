package com.shuanglin.framework.milky.model.request;
import lombok.AllArgsConstructor;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 设置QQ账号头像请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SetAvatarRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 头像URI
     */
    private String uri;
}
