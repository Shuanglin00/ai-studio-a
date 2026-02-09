package com.shuanglin.framework.milky.enums;

/**
 * 群成员角色枚举
 */
public enum GroupRole {
    OWNER("owner"),
    ADMIN("admin"),
    MEMBER("member");

    private final String value;

    GroupRole(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static GroupRole fromValue(String value) {
        for (GroupRole role : values()) {
            if (role.value.equals(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + value);
    }
}
