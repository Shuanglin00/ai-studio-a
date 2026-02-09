package com.shuanglin.framework.milky.model.request;
import lombok.AllArgsConstructor;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 获取群荣誉信息请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetGroupHonorRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 群ID
     */
    private Long groupId;

    /**
     * 荣誉类型
     */
    private String honorType;
}
