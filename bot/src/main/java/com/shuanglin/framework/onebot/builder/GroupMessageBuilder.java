package com.shuanglin.framework.onebot.builder;

import com.shuanglin.framework.onebot.model.Message;
import com.shuanglin.framework.onebot.model.MessageResponse;
import com.shuanglin.framework.onebot.sender.MessageSender;
import lombok.extern.slf4j.Slf4j;

/**
 * 群聊消息构建器
 */
@Slf4j
public class GroupMessageBuilder extends MessageBuilder {

    private final String groupId;
    private static MessageSender messageSender;

    /**
     * 设置全局MessageSender（由Spring容器注入）
     */
    public static void setMessageSender(MessageSender sender) {
        messageSender = sender;
    }

    public GroupMessageBuilder(String groupId) {
        this.groupId = groupId;
    }

    /**
     * 静态工厂方法
     */
    public static GroupMessageBuilder forGroup(String groupId) {
        return new GroupMessageBuilder(groupId);
    }

    @Override
    public Message build() {
        if (segments.isEmpty()) {
            throw new IllegalStateException("Message cannot be empty");
        }

        return Message.builder()
                .targetId(groupId)
                .segments(segments)
                .build();
    }

    @Override
    public MessageResponse send() {
        Message message = build();
        
        if (messageSender == null) {
            throw new IllegalStateException("MessageSender not initialized");
        }
        
        return messageSender.sendGroupMessage(message);
    }
}
