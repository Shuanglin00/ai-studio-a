package com.shuanglin.framework.onebot.segment;

import lombok.Getter;

/**
 * Markdown消息段
 */
@Getter
public class MarkdownSegment extends MessageSegment {

    public MarkdownSegment(String content) {
        this.type = "markdown";
        this.data.put("content", content);
    }

    @Override
    public void validate() {
        if (data.get("content") == null) {
            throw new IllegalArgumentException("Markdown content cannot be null");
        }
    }
}
