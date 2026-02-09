package com.shuanglin.framework.enums.onebot;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * OneBot 消息段类型枚举
 */
@Getter
@AllArgsConstructor
public enum SegmentType {
    TEXT("text"),
    IMAGE("image"),
    FACE("face"),
    MFACE("mface"),
    REPLY("reply"),
    AT("at"),
    RECORD("record"),
    VIDEO("video"),
    FILE("file"),
    JSON("json"),
    MARKDOWN("markdown"),
    FORWARD("forward"),
    DICE("dice"),
    RPS("rps"),
    KEYBOARD("keyboard");

    private final String value;

    /**
     * 根据字符串值获取枚举
     */
    public static SegmentType fromValue(String value) {
        for (SegmentType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown segment type: " + value);
    }
}
