package com.shuanglin.framework.onebot.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.shuanglin.framework.onebot.segment.MessageSegment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 消息对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Message {

    /**
     * 群号或用户ID
     */
    private String targetId;

    /**
     * 消息段列表
     */
    @Builder.Default
    private List<MessageSegment> segments = new ArrayList<>();

    /**
     * 转换为JSON
     */
    public JsonArray toJsonArray() {
        JsonArray array = new JsonArray();
        for (MessageSegment segment : segments) {
            array.add(segment.toJson());
        }
        return array;
    }
}
