package com.shuanglin.framework.onebot.service;

import com.shuanglin.framework.onebot.builder.GroupMessageBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Bot消息服务
 * 提供统一的消息发送接口
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BotMessageService {

    /**
     * 发送文本消息
     */
    public void sendText(String groupId, String text) {
        GroupMessageBuilder.forGroup(groupId)
                .text(text)
                .send();
    }

    /**
     * 发送回复消息
     */
    public void sendReply(String groupId, Long messageId, String text) {
        GroupMessageBuilder.forGroup(groupId)
                .reply(messageId)
                .text(text)
                .send();
    }

    /**
     * 发送@消息
     */
    public void sendAtMessage(String groupId, String userId, String text) {
        GroupMessageBuilder.forGroup(groupId)
                .at(userId)
                .text(text)
                .send();
    }

    /**
     * 发送权限不足提示
     */
    public void sendPermissionDenied(String groupId, String userId, Long messageId) {
        GroupMessageBuilder.forGroup(groupId)
                .reply(messageId)
                .at(userId)
                .text(" 权限不足，需要管理员权限")
                .send();
    }

    /**
     * 发送AI回复
     */
    public void sendAiResponse(String groupId, Long replyToMessageId, String answer) {
        GroupMessageBuilder.forGroup(groupId)
                .reply(replyToMessageId)
                .text(answer)
                .send();
    }
}
