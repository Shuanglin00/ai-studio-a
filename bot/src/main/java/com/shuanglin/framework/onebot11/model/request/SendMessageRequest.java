package com.shuanglin.framework.onebot11.model.request;

import com.shuanglin.framework.onebot11.exception.ValidationException;
import lombok.Builder;
import lombok.Data;

/**
 * 发送消息请求（通用接口）
 */
@Data
@Builder
public class SendMessageRequest implements ValidatableRequest {

    /**
     * 消息类型：private 或 group
     */
    private String messageType;

    /**
     * 对方 QQ 号（私聊时必填）
     */
    private Long userId;

    /**
     * 群号（群聊时必填）
     */
    private Long groupId;

    /**
     * 消息内容
     */
    private Object message;

    /**
     * 是否自动转义
     */
    @Builder.Default
    private Boolean autoEscape = false;

    @Override
    public void validate() {
        if (messageType == null || messageType.isEmpty()) {
            throw new ValidationException("消息类型不能为空", "messageType");
        }
        if (!"private".equals(messageType) && !"group".equals(messageType)) {
            throw new ValidationException("消息类型必须是 private 或 group", "messageType");
        }
        if ("private".equals(messageType) && (userId == null || userId <= 0)) {
            throw new ValidationException("私聊时 userId 必须大于 0", "userId");
        }
        if ("group".equals(messageType) && (groupId == null || groupId <= 0)) {
            throw new ValidationException("群聊时 groupId 必须大于 0", "groupId");
        }
        if (message == null) {
            throw new ValidationException("消息内容不能为空", "message");
        }
    }
}
