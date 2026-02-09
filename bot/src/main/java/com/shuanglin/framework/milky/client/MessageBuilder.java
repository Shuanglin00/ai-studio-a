package com.shuanglin.framework.milky.client;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息段构建器，用于方便地构建消息内容
 */
@Data
@Builder
public class MessageBuilder implements Serializable {
    private static final long serialVersionUID = 1L;

    private final List<Map<String, Object>> segments = new ArrayList<>();

    /**
     * 添加文本消息段
     */
    public MessageBuilder text(String text) {
        Map<String, Object> segment = new HashMap<>();
        segment.put("type", "text");
        Map<String, Object> data = new HashMap<>();
        data.put("text", text);
        segment.put("data", data);
        segments.add(segment);
        return this;
    }

    /**
     * 添加@消息段
     */
    public MessageBuilder at(Long userId) {
        Map<String, Object> segment = new HashMap<>();
        segment.put("type", "at");
        Map<String, Object> data = new HashMap<>();
        data.put("qq", userId);
        segment.put("data", data);
        segments.add(segment);
        return this;
    }

    /**
     * 添加@全体消息段
     */
    public MessageBuilder atAll() {
        Map<String, Object> segment = new HashMap<>();
        segment.put("type", "at");
        Map<String, Object> data = new HashMap<>();
        data.put("qq", "all");
        segment.put("data", data);
        segments.add(segment);
        return this;
    }

    /**
     * 添加表情消息段
     */
    public MessageBuilder face(Integer faceId) {
        Map<String, Object> segment = new HashMap<>();
        segment.put("type", "face");
        Map<String, Object> data = new HashMap<>();
        data.put("id", faceId);
        segment.put("data", data);
        segments.add(segment);
        return this;
    }

    /**
     * 添加图片消息段（通过URL）
     */
    public MessageBuilder image(String url) {
        Map<String, Object> segment = new HashMap<>();
        segment.put("type", "image");
        Map<String, Object> data = new HashMap<>();
        data.put("url", url);
        segment.put("data", data);
        segments.add(segment);
        return this;
    }

    /**
     * 添加图片消息段（通过Base64）
     */
    public MessageBuilder imageBase64(String base64) {
        Map<String, Object> segment = new HashMap<>();
        segment.put("type", "image");
        Map<String, Object> data = new HashMap<>();
        data.put("data", base64);
        segment.put("data", data);
        segments.add(segment);
        return this;
    }

    /**
     * 添加图片消息段（通过文件路径）
     */
    public MessageBuilder imageFile(String file) {
        Map<String, Object> segment = new HashMap<>();
        segment.put("type", "image");
        Map<String, Object> data = new HashMap<>();
        data.put("file", file);
        segment.put("data", data);
        segments.add(segment);
        return this;
    }

    /**
     * 添加语音消息段
     */
    public MessageBuilder record(String url) {
        Map<String, Object> segment = new HashMap<>();
        segment.put("type", "record");
        Map<String, Object> data = new HashMap<>();
        data.put("file", url);
        segment.put("data", data);
        segments.add(segment);
        return this;
    }

    /**
     * 添加视频消息段
     */
    public MessageBuilder video(String url) {
        Map<String, Object> segment = new HashMap<>();
        segment.put("type", "video");
        Map<String, Object> data = new HashMap<>();
        data.put("file", url);
        segment.put("data", data);
        segments.add(segment);
        return this;
    }

    /**
     * 添加文件消息段
     */
    public MessageBuilder file(String url, String name) {
        Map<String, Object> segment = new HashMap<>();
        segment.put("type", "file");
        Map<String, Object> data = new HashMap<>();
        data.put("file", url);
        data.put("name", name);
        segment.put("data", data);
        segments.add(segment);
        return this;
    }

    /**
     * 添加回复消息段
     */
    public MessageBuilder reply(Long messageId) {
        Map<String, Object> segment = new HashMap<>();
        segment.put("type", "reply");
        Map<String, Object> data = new HashMap<>();
        data.put("id", messageId);
        segment.put("data", data);
        segments.add(segment);
        return this;
    }

    /**
     * 添加自定义消息段
     */
    public MessageBuilder addSegment(String type, Map<String, Object> data) {
        Map<String, Object> segment = new HashMap<>();
        segment.put("type", type);
        segment.put("data", data);
        segments.add(segment);
        return this;
    }

    /**
     * 构建消息段列表
     */
    public List<Map<String, Object>> build() {
        return new ArrayList<>(segments);
    }

    /**
     * 创建新的消息构建器
     */
    public static MessageBuilder create() {
        return new MessageBuilder();
    }
}
