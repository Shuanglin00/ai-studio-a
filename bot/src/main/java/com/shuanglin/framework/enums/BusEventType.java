package com.shuanglin.framework.enums;

/**
 * 消息总线事件类型
 * 用于 @PublishBus 注解标识事件类型，下游消费者可据此过滤
 */
public enum BusEventType {
    /**
     * Webhook 事件（OneBot 推送的原始事件）
     */
    WEBHOOK("webhook"),

    /**
     * 内部自定义事件
     */
    INTERNAL("internal");

    private final String value;

    BusEventType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static BusEventType fromValue(String value) {
        for (BusEventType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return WEBHOOK; // 默认值
    }
}
