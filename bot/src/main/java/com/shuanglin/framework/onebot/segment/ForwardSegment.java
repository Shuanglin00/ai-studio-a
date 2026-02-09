package com.shuanglin.framework.onebot.segment;

import lombok.Getter;

/**
 * 转发消息段
 */
@Getter
public class ForwardSegment extends MessageSegment {

    public ForwardSegment(String id) {
        this.type = "forward";
        this.data.put("id", id);
    }

    @Override
    public void validate() {
        if (data.get("id") == null) {
            throw new IllegalArgumentException("Forward message ID cannot be null");
        }
    }
}
