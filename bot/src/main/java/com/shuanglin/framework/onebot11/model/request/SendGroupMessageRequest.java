package com.shuanglin.framework.onebot11.model.request;

import com.shuanglin.framework.onebot11.exception.ValidationException;
import lombok.Builder;
import lombok.Data;

/**
 * 发送群消息请求
 */
@Data
@Builder
public class SendGroupMessageRequest implements ValidatableRequest {

    /**
     * 群号
     */
    private Long groupId;

    /**
     * 消息内容（消息段数组或纯文本）
     */
    private Object message;

    /**
     * 是否自动转义（默认为 false）
     */
    @Builder.Default
    private Boolean autoEscape = false;

    @Override
    public void validate() {
        if (groupId == null || groupId <= 0) {
            throw new ValidationException("groupId 必须大于 0", "groupId");
        }
        if (message == null) {
            throw new ValidationException("消息内容不能为空", "message");
        }
    }
}
