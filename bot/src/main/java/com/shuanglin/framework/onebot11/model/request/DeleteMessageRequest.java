package com.shuanglin.framework.onebot11.model.request;

import com.shuanglin.framework.onebot11.exception.ValidationException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 撤回消息请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeleteMessageRequest implements ValidatableRequest {

    /**
     * 消息 ID
     */
    private Long messageId;

    @Override
    public void validate() {
        if (messageId == null || messageId <= 0) {
            throw new ValidationException("messageId 必须大于 0", "messageId");
        }
    }
}
