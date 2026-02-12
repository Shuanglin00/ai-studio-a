package com.shuanglin.framework.onebot11.model.request;

import com.shuanglin.framework.onebot11.exception.ValidationException;
import lombok.Builder;
import lombok.Data;

/**
 * 群组禁言请求
 */
@Data
@Builder
public class SetGroupBanRequest implements ValidatableRequest {

    /**
     * 群号
     */
    private Long groupId;

    /**
     * 要禁言的 QQ 号
     */
    private Long userId;

    /**
     * 禁言时长（秒），0 表示取消禁言
     */
    @Builder.Default
    private Integer duration = 1800;

    @Override
    public void validate() {
        if (groupId == null || groupId <= 0) {
            throw new ValidationException("groupId 必须大于 0", "groupId");
        }
        if (userId == null || userId <= 0) {
            throw new ValidationException("userId 必须大于 0", "userId");
        }
        if (duration < 0) {
            throw new ValidationException("禁言时长不能为负数", "duration");
        }
    }
}
