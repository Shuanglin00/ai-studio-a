package com.shuanglin.framework.onebot11.model.request;

import com.shuanglin.framework.onebot11.exception.ValidationException;
import lombok.Builder;
import lombok.Data;

/**
 * 退出群组请求
 */
@Data
@Builder
public class SetGroupLeaveRequest implements ValidatableRequest {

    /**
     * 群号
     */
    private Long groupId;

    /**
     * 是否解散，如果登录号是群主则有效
     */
    @Builder.Default
    private Boolean isDismiss = false;

    @Override
    public void validate() {
        if (groupId == null || groupId <= 0) {
            throw new ValidationException("groupId 必须大于 0", "groupId");
        }
    }
}
