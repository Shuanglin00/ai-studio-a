package com.shuanglin.framework.milky.model.request;
import lombok.AllArgsConstructor;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 获取群成员列表请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupMemberListRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 群ID
     */
    private Long groupId;

    /**
     * 是否不使用缓存
     */
    private Boolean noCache;
}
