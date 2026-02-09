package com.shuanglin.framework.milky.enums;

/**
 * 消息子类型枚举
 */
public enum MessageSubType {
    NORMAL("normal"),
    STICKER("sticker");

    private final String value;

    MessageSubType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static MessageSubType fromValue(String value) {
        for (MessageSubType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown message sub type: " + value);
    }
}
