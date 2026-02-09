package com.shuanglin.framework.milky.model.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;

/**
 * 设置用户头衔请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SetUserTitleRequest implements Serializable {
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
     * 新头衔
     */
    private String title;
}
