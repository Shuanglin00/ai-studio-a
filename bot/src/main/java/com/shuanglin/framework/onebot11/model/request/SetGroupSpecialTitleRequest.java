package com.shuanglin.framework.onebot11.model.request;

import com.shuanglin.framework.onebot11.exception.ValidationException;
import lombok.Builder;
import lombok.Data;

/**
 * 设置群组专属头衔请求
 */
@Data
@Builder
public class SetGroupSpecialTitleRequest implements ValidatableRequest {

    /**
     * 群号
     */
    private Long groupId;

    /**
     * 要设置的 QQ 号
     */
    private Long userId;

    /**
     * 专属头衔，为空则取消
     */
    private String specialTitle;

    /**
     * 有效期（秒），-1 表示永久
     */
    @Builder.Default
    private Integer duration = -1;

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
