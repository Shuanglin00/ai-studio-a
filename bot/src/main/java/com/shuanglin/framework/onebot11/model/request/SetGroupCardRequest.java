package com.shuanglin.framework.onebot11.model.request;

import com.shuanglin.framework.onebot11.exception.ValidationException;
import lombok.Builder;
import lombok.Data;

/**
 * 设置群名片请求
 */
@Data
@Builder
public class SetGroupCardRequest implements ValidatableRequest {

    /**
     * 群号
     */
    private Long groupId;

    /**
     * 要设置的 QQ 号
     */
    private Long userId;

    /**
     * 群名片内容，为空则取消群名片
     */
    private String card;

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
