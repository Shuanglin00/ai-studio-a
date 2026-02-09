package com.shuanglin.framework.milky.model.request;
import lombok.AllArgsConstructor;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 设置QQ账号个性签名请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SetBioRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 新个性签名
     */
    private String newBio;
}
