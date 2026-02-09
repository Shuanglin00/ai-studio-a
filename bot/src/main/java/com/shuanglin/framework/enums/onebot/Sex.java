package com.shuanglin.framework.enums.onebot;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 性别枚举
 */
@Getter
@AllArgsConstructor
public enum Sex {
    MALE("male"),
    FEMALE("female"),
    UNKNOWN("unknown");

    private final String value;
}
