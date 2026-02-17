package com.shuanglin.dao.classification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 消息段（用于存储图片、表情、@等富文本内容）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageSegment {

    /**
     * 段类型：text, image, at, face, etc.
     */
    private String type;

    /**
     * 段数据
     */
    private Map<String, Object> data;

    /**
     * 创建文本段
     */
    public static MessageSegment text(String text) {
        return MessageSegment.builder()
                .type("text")
                .data(Map.of("text", text))
                .build();
    }

    /**
     * 创建@用户段
     */
    public static MessageSegment at(Long userId) {
        return MessageSegment.builder()
                .type("at")
                .data(Map.of("qq", userId))
                .build();
    }

    /**
     * 创建图片段
     */
    public static MessageSegment image(String url) {
        return MessageSegment.builder()
                .type("image")
                .data(Map.of("url", url))
                .build();
    }
}
