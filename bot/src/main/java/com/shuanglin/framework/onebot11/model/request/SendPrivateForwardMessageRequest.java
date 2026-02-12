package com.shuanglin.framework.onebot11.model.request;

import com.shuanglin.framework.onebot11.exception.ValidationException;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 发送私聊合并转发消息请求
 */
@Data
@Builder
public class SendPrivateForwardMessageRequest implements ValidatableRequest {

    /**
     * 对方 QQ 号
     */
    private Long userId;

    /**
     * 自定义转发消息节点
     */
    private List<ForwardNode> messages;

    @Override
    public void validate() {
        if (userId == null || userId <= 0) {
            throw new ValidationException("userId 必须大于 0", "userId");
        }
        if (messages == null || messages.isEmpty()) {
            throw new ValidationException("转发消息列表不能为空", "messages");
        }
    }

    /**
     * 转发消息节点
     */
    @Data
    @Builder
    public static class ForwardNode {
        /**
         * 转发消息类型（node）
         */
        private String type;

        /**
         * 节点数据
         */
        private ForwardNodeData data;
    }

    /**
     * 转发节点数据
     */
    @Data
    @Builder
    public static class ForwardNodeData {
        /**
         * 转发的消息 ID
         */
        private Long id;

        /**
         * 发送者 QQ 号（自定义消息时）
         */
        private Long uin;

        /**
         * 发送者昵称（自定义消息时）
         */
        private String name;

        /**
         * 消息内容（自定义消息时）
         */
        private Object content;
    }
}
