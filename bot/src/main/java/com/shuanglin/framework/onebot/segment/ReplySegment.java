package com.shuanglin.framework.onebot.segment;

/**
 * 回复消息段
 */
public class ReplySegment extends MessageSegment {

    public ReplySegment(Long messageId) {
        this.type = "reply";
        this.data.put("id", messageId);
    }

    @Override
    public void validate() {
        if (data.get("id") == null) {
            throw new IllegalArgumentException("Message ID cannot be null");
        }
    }
}
