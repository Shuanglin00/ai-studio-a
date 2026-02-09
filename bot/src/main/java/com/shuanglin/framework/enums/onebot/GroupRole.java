package com.shuanglin.framework.enums.onebot;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 群成员角色枚举
 */
@Getter
@AllArgsConstructor
public enum GroupRole {
    OWNER("owner"),
    ADMIN("admin"),
    MEMBER("member");

    private final String value;
}
