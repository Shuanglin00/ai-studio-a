package com.shuanglin.framework.enums.onebot;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * OneBot 消息类型枚举
 */
@Getter
@AllArgsConstructor
public enum MessageType {
    PRIVATE("private"),
    GROUP("group");

    private final String value;
}
