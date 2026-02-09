package com.shuanglin.framework.milky.model.request;
import lombok.AllArgsConstructor;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 设置QQ账号昵称请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SetNicknameRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 新昵称
     */
    private String newNickname;
}
