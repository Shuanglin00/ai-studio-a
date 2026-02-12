package com.shuanglin.framework.onebot11.model.request;

import com.shuanglin.framework.onebot11.exception.ValidationException;
import lombok.Builder;
import lombok.Data;

/**
 * 发送私聊消息请求
 */
@Data
@Builder
public class SendPrivateMessageRequest implements ValidatableRequest {

    /**
     * 对方 QQ 号
     */
    private Long userId;

    /**
     * 对方 QQ 号（可选，与 user_id 二选一）
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
        if (userId == null || userId <= 0) {
            throw new ValidationException("userId 必须大于 0", "userId");
        }
        if (message == null) {
            throw new ValidationException("消息内容不能为空", "message");
        }
    }
}
