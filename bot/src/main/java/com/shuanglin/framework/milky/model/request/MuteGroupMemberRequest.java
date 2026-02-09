package com.shuanglin.framework.milky.model.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;

/**
 * 群成员禁言请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MuteGroupMemberRequest implements Serializable {
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
     * 禁言时长（秒）
     */
    private Long duration;
}
