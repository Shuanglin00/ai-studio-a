package com.shuanglin.framework.milky.enums;

/**
 * 消息类型枚举
 */
public enum MessageType {
    TEXT("text"),
    MENTION("mention"),
    MENTION_ALL("mention_all"),
    FACE("face"),
    REPLY("reply"),
    IMAGE("image"),
    RECORD("record"),
    VIDEO("video"),
    FILE("file"),
    FORWARD("forward"),
    MARKET_FACE("market_face"),
    LIGHT_APP("light_app"),
    XML("xml");

    private final String value;

    MessageType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static MessageType fromValue(String value) {
        for (MessageType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown message type: " + value);
    }
}
