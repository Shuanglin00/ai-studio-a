package com.shuanglin.framework.milky.enums;

/**
 * 性别枚举
 */
public enum Sex {
    MALE("male"),
    FEMALE("female"),
    UNKNOWN("unknown");

    private final String value;

    Sex(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Sex fromValue(String value) {
        for (Sex sex : values()) {
            if (sex.value.equals(value)) {
                return sex;
            }
        }
        throw new IllegalArgumentException("Unknown sex: " + value);
    }
}
