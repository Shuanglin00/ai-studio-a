package com.shuanglin.framework.milky.model.request;
import lombok.AllArgsConstructor;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 获取群邀请请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetGroupInvitationRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 群ID
     */
    private Long groupId;
}
