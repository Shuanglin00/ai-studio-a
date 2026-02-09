package com.shuanglin.framework.onebot.segment;

/**
 * 文本消息段
 */
public class TextSegment extends MessageSegment {

    public TextSegment(String text) {
        this.type = "text";
        this.data.put("text", text);
    }

    @Override
    public void validate() {
        if (data.get("text") == null || data.get("text").toString().isEmpty()) {
            throw new IllegalArgumentException("Text cannot be empty");
        }
    }
}
