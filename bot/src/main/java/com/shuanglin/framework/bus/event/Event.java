package com.shuanglin.framework.bus.event;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.gson.annotations.SerializedName;
import com.shuanglin.framework.enums.onebot.PostType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 事件基类
 * 存储原始数据并提供类型安全的事件对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "post_type_detail"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = GroupMessageEvent.class, name = "groupMessageEvent"),
})
public class Event implements Serializable {

    @SerializedName("time")
    private Long time;         // 事件发生的时间戳（Unix 时间戳，秒）

    @SerializedName("self_id")
    private Long selfId;       // 收到事件的机器人 QQ 号

    @SerializedName("post_type")
    private PostType postType; // 事件类型 message/message_sent/notice/request/meta_event

    // 原始数据缓存，用于 get() 方法
    private Map<String, Object> rawData;

    /**
     * 设置原始数据（由反序列化器调用）
     */
    public void setRawData(Map<String, Object> rawData) {
        this.rawData = rawData != null ? new HashMap<>(rawData) : new HashMap<>();
    }

    /**
     * 获取原始 JSON 数据的指定字段值
     *
     * @param key 字段名
     * @return 字段值，不存在时返回 null
     */
    public Object get(String key) {
        if (rawData != null) {
            return rawData.get(key);
        }
        return null;
    }
}
