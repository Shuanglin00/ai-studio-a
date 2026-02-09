package com.shuanglin.framework.onebot.segment;

import lombok.Getter;

/**
 * 表情消息段
 */
@Getter
public class FaceSegment extends MessageSegment {

    public FaceSegment(String id) {
        this.type = "face";
        this.data.put("id", id);
    }

    @Override
    public void validate() {
        if (data.get("id") == null) {
            throw new IllegalArgumentException("Face ID cannot be null");
        }
    }
}
