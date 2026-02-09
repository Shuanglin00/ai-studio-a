package com.shuanglin.framework.milky.model.request;
import lombok.AllArgsConstructor;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 全体禁言请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MuteGroupAllRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 群ID
     */
    private Long groupId;

    /**
     * 是否开启全体禁言
     */
    private Boolean enable;
}
