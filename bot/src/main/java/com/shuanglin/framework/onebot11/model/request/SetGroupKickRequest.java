package com.shuanglin.framework.onebot11.model.request;

import com.shuanglin.framework.onebot11.exception.ValidationException;
import lombok.Builder;
import lombok.Data;

/**
 * 群组踢人请求
 */
@Data
@Builder
public class SetGroupKickRequest implements ValidatableRequest {

    /**
     * 群号
     */
    private Long groupId;

    /**
     * 要踢的 QQ 号
     */
    private Long userId;

    /**
     * 是否拒绝再次加群
     */
    @Builder.Default
    private Boolean rejectAddRequest = false;

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
