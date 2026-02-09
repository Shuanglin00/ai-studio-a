package com.shuanglin.framework.milky.model.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;

/**
 * 群成员踢出请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KickGroupMemberRequest implements Serializable {
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
     * 是否拒绝再次申请
     */
    private Boolean rejectAddRequest;
}
