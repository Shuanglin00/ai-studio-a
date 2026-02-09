package com.shuanglin.framework.onebot.builder;

import com.shuanglin.framework.onebot.model.Message;
import com.shuanglin.framework.onebot.model.MessageResponse;
import com.shuanglin.framework.onebot.segment.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 消息构建器抽象类
 * 提供链式API构建消息段列表
 */
public abstract class MessageBuilder {

    protected List<MessageSegment> segments = new ArrayList<>();

    /**
     * 添加文本消息段
     */
    public MessageBuilder text(String content) {
        segments.add(new TextSegment(content));
        return this;
    }

    /**
     * 添加图片消息段
     */
    public MessageBuilder image(String filePathOrUrl) {
        segments.add(new ImageSegment(filePathOrUrl));
        return this;
    }

    /**
     * 添加@消息段
     */
    public MessageBuilder at(String qq) {
        segments.add(new AtSegment(qq));
        return this;
    }

    /**
     * 添加@全体成员
     */
    public MessageBuilder atAll() {
        segments.add(AtSegment.atAll());
        return this;
    }

    /**
     * 添加回复消息段
     */
    public MessageBuilder reply(Long messageId) {
        segments.add(new ReplySegment(messageId));
        return this;
    }

    /**
     * 构建最终消息对象
     */
    public abstract Message build();

    /**
     * 构建并发送消息
     */
    public abstract MessageResponse send();
}
