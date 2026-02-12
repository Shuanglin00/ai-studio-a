package com.shuanglin.framework.onebot11.model.request;

import com.shuanglin.framework.onebot11.exception.ValidationException;
import lombok.Builder;
import lombok.Data;

/**
 * 群组全员禁言请求
 */
@Data
@Builder
public class SetGroupWholeBanRequest implements ValidatableRequest {

    /**
     * 群号
     */
    private Long groupId;

    /**
     * 是否禁言
     */
    @Builder.Default
    private Boolean enable = true;

    @Override
    public void validate() {
        if (groupId == null || groupId <= 0) {
            throw new ValidationException("groupId 必须大于 0", "groupId");
        }
    }
}
