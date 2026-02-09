package com.shuanglin.framework.milky.enums;

/**
 * QQ协议类型枚举
 */
public enum QqProtocolType {
    WINDOWS("windows"),
    LINUX("linux"),
    MACOS("macos"),
    ANDROID_PAD("android_pad"),
    ANDROID_PHONE("android_phone"),
    IPAD("ipad"),
    IPHONE("iphone"),
    HARMONY("harmony"),
    WATCH("watch");

    private final String value;

    QqProtocolType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static QqProtocolType fromValue(String value) {
        for (QqProtocolType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown QQ protocol type: " + value);
    }
}
