package com.shuanglin.framework.milky.model.request;
import lombok.AllArgsConstructor;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 获取好友列表请求（群成员）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetGroupMembersRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 群ID
     */
    private Long groupId;
}
