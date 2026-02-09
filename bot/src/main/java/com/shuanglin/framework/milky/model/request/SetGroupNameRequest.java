package com.shuanglin.framework.milky.model.request;
import lombok.AllArgsConstructor;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 设置群名称请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SetGroupNameRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 群ID
     */
    private Long groupId;

    /**
     * 新群名称
     */
    private String groupName;
}
