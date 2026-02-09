package com.shuanglin.framework.onebot.segment;

import lombok.Getter;

/**
 * JSON消息段
 */
@Getter
public class JsonSegment extends MessageSegment {

    public JsonSegment(String data) {
        this.type = "json";
        this.data.put("data", data);
    }

    @Override
    public void validate() {
        if (data.get("data") == null) {
            throw new IllegalArgumentException("JSON data cannot be null");
        }
    }
}
