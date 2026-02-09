package com.shuanglin.framework.enums.onebot;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * OneBot 消息子类型枚举
 */
@Getter
@AllArgsConstructor
public enum SubType {
    FRIEND("friend"),
    GROUP("group"),
    NORMAL("normal");

    private final String value;
}
