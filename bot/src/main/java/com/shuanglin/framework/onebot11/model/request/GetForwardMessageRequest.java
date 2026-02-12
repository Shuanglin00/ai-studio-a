package com.shuanglin.framework.onebot11.model.request;

import com.shuanglin.framework.onebot11.exception.ValidationException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 获取合并转发消息内容请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetForwardMessageRequest implements ValidatableRequest {

    /**
     * 合并转发消息 ID
     */
    private String messageId;

    @Override
    public void validate() {
        if (messageId == null || messageId.isEmpty()) {
            throw new ValidationException("messageId 不能为空", "messageId");
        }
    }
}
