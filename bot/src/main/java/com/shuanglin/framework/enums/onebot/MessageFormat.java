package com.shuanglin.framework.enums.onebot;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消息格式枚举
 */
@Getter
@AllArgsConstructor
public enum MessageFormat {
    ARRAY("array"),
    STRING("string");

    private final String value;
}
