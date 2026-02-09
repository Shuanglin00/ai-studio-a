package com.shuanglin.framework.milky.model.response;

import lombok.Data;

import java.io.Serializable;

/**
 * 群信息
 */
@Data
public class GroupInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 群ID
     */
    private Long groupId;

    /**
     * 群名称
     */
    private String groupName;

    /**
     * 成员数量
     */
    private Integer memberCount;

    /**
     * 最大成员数量
     */
    private Integer maxMemberCount;
}
