package com.shuanglin.framework.enums.onebot;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * OneBot 事件类型枚举
 */
@Getter
@AllArgsConstructor
public enum PostType {
    MESSAGE("message"),
    MESSAGE_SENT("message_sent"),
    NOTICE("notice"),
    REQUEST("request"),
    META_EVENT("meta_event");

    private final String value;
}
