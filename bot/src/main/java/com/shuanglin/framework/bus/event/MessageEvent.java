package com.shuanglin.framework.bus.event;

import com.google.gson.annotations.SerializedName;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.shuanglin.framework.bus.event.data.Sender;
import com.shuanglin.framework.enums.onebot.MessageFormat;
import com.shuanglin.framework.enums.onebot.MessageType;
import com.shuanglin.framework.enums.onebot.SubType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 消息事件基类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MessageEvent extends Event implements Serializable {

    @SerializedName("message_id")
    private Long messageId;       // 消息 ID（短 ID）

    @SerializedName("message_seq")
    private Long messageSeq;      // 消息序列号

    @SerializedName("real_id")
    private Long realId;          // 真实消息 ID（仅在 get_msg 接口存在）

    @SerializedName("user_id")
    private Long userId;          // 发送者的 QQ 号

    @SerializedName("group_id")
    private String groupId;       // 群号（仅群消息）

    @SerializedName("message_type")
    private MessageType messageType;  // 消息类型 private/group

    @SerializedName("sub_type")
    private SubType subType;      // 消息子类型 friend/group/normal

    @SerializedName("sender")
    private Sender sender;        // 发送者信息

    @SerializedName("message")
    private Object message;       // 消息内容（message_format=array 时是数组，string 时是字符串）

    @SerializedName("message_format")
    private MessageFormat messageFormat;   // 消息格式类型 array/string

    @SerializedName("raw_message")
    private String rawMessage;    // 原始消息内容（CQ 码格式）

    @SerializedName("font")
    private Long font;            // 字体 ID，默认 14

    @SerializedName("target_id")
    private Long targetId;        // 目标 ID（仅发送的消息）

    @SerializedName("temp_source")
    private Integer tempSource;   // 临时聊天来源（0 = 群聊）

    /**
     * 获取消息的纯文本内容
     * 支持 message_format 为 array（消息段数组）和 string（纯文本）两种格式
     */
    @SuppressWarnings("unchecked")
    public String getMessageText() {
        if (message == null) {
            return "";
        }

        // 如果是字符串格式，直接返回
        if (message instanceof String) {
            return (String) message;
        }

        // 如果是数组格式，提取文本段
        // GSON 反序列化后，List 中的元素是 LinkedTreeMap 或 JsonObject
        if (message instanceof List) {
            StringBuilder sb = new StringBuilder();
            for (Object segment : (List<?>) message) {
                if (segment instanceof JsonObject) {
                    JsonObject obj = (JsonObject) segment;
                    JsonElement typeElem = obj.get("type");
                    if (typeElem != null && "text".equals(typeElem.getAsString())) {
                        JsonElement textElem = obj.getAsJsonObject("data").get("text");
                        if (textElem != null) {
                            sb.append(textElem.getAsString());
                        }
                    }
                } else if (segment instanceof java.util.Map) {
                    // 处理 LinkedTreeMap 情况
                    java.util.Map<String, Object> map = (java.util.Map<String, Object>) segment;
                    Object type = map.get("type");
                    if ("text".equals(type)) {
                        Object data = map.get("data");
                        if (data instanceof java.util.Map) {
                            Object text = ((java.util.Map<String, Object>) data).get("text");
                            if (text != null) {
                                sb.append(text.toString());
                            }
                        }
                    }
                }
            }
            return sb.toString();
        }

        return "";
    }

    /**
     * 判断是否是群消息
     */
    public boolean isGroupMessage() {
        return MessageType.GROUP.equals(messageType);
    }

    /**
     * 判断是否是私聊消息
     */
    public boolean isPrivateMessage() {
        return MessageType.PRIVATE.equals(messageType);
    }
}
