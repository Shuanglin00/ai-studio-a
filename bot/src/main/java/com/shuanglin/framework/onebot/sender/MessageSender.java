package com.shuanglin.framework.onebot.sender;

import com.shuanglin.framework.onebot.model.Message;
import com.shuanglin.framework.onebot.model.MessageResponse;

/**
 * 消息发送器接口
 */
public interface MessageSender {

    /**
     * 发送群聊消息
     */
    MessageResponse sendGroupMessage(Message message);

    /**
     * 发送私聊消息
     */
    MessageResponse sendPrivateMessage(Message message);
}
