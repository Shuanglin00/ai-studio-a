package com.shuanglin.framework.onebot.segment;

import com.google.gson.JsonObject;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 消息段抽象基类
 * OneBot11消息段封装
 */
@Data
public abstract class MessageSegment {

    /**
     * 消息段类型
     */
    protected String type;

    /**
     * 消息段数据
     */
    protected Map<String, Object> data = new HashMap<>();

    /**
     * 数据验证（子类实现）
     */
    public abstract void validate();

    /**
     * 转换为JSON对象
     */
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", type);
        
        JsonObject dataJson = new JsonObject();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (entry.getValue() instanceof String) {
                dataJson.addProperty(entry.getKey(), (String) entry.getValue());
            } else if (entry.getValue() instanceof Number) {
                dataJson.addProperty(entry.getKey(), (Number) entry.getValue());
            } else if (entry.getValue() instanceof Boolean) {
                dataJson.addProperty(entry.getKey(), (Boolean) entry.getValue());
            }
        }
        json.add("data", dataJson);
        
        return json;
    }
}
