package com.shuanglin.framework.milky.enums;

/**
 * 消息场景枚举
 */
public enum MessageScene {
    FRIEND("friend"),
    GROUP("group"),
    TEMP("temp");

    private final String value;

    MessageScene(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static MessageScene fromValue(String value) {
        for (MessageScene scene : values()) {
            if (scene.value.equals(value)) {
                return scene;
            }
        }
        throw new IllegalArgumentException("Unknown message scene: " + value);
    }
}
