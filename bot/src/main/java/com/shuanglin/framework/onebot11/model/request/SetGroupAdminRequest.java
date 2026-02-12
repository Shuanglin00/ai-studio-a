package com.shuanglin.framework.onebot11.model.request;

import com.shuanglin.framework.onebot11.exception.ValidationException;
import lombok.Builder;
import lombok.Data;

/**
 * 设置群管理员请求
 */
@Data
@Builder
public class SetGroupAdminRequest implements ValidatableRequest {

    /**
     * 群号
     */
    private Long groupId;

    /**
     * 要设置的 QQ 号
     */
    private Long userId;

    /**
     * true 为设置，false 为取消
     */
    @Builder.Default
    private Boolean enable = true;

    @Override
    public void validate() {
        if (groupId == null || groupId <= 0) {
            throw new ValidationException("groupId 必须大于 0", "groupId");
        }
        if (userId == null || userId <= 0) {
            throw new ValidationException("userId 必须大于 0", "userId");
        }
    }
}
