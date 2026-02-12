package com.shuanglin.framework.onebot11.model.request;

import com.shuanglin.framework.onebot11.exception.ValidationException;
import lombok.Builder;
import lombok.Data;

/**
 * 设置在线状态请求
 */
@Data
@Builder
public class SetOnlineStatusRequest implements ValidatableRequest {

    /**
     * 在线状态
     * online: 在线
     * away: 离开
     * hidden: 隐身
     * busy: 忙碌
     */
    private String status;

    @Override
    public void validate() {
        if (status == null || status.isEmpty()) {
            throw new ValidationException("状态不能为空", "status");
        }
        if (!status.matches("online|away|hidden|busy")) {
            throw new ValidationException("状态必须是 online/away/hidden/busy 之一", "status");
        }
    }
}
