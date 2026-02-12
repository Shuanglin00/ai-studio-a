package com.shuanglin.framework.onebot11.model.request;

import com.shuanglin.framework.onebot11.exception.ValidationException;
import lombok.Builder;
import lombok.Data;

/**
 * 设置群名请求
 */
@Data
@Builder
public class SetGroupNameRequest implements ValidatableRequest {

    /**
     * 群号
     */
    private Long groupId;

    /**
     * 新群名
     */
    private String groupName;

    @Override
    public void validate() {
        if (groupId == null || groupId <= 0) {
            throw new ValidationException("groupId 必须大于 0", "groupId");
        }
        if (groupName == null || groupName.isEmpty()) {
            throw new ValidationException("群名不能为空", "groupName");
        }
    }
}
