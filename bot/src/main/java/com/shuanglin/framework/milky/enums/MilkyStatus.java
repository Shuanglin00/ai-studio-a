package com.shuanglin.framework.milky.enums;

/**
 * Milky API 响应状态
 */
public enum MilkyStatus {
    OK("ok"),
    FAILED("failed");

    private final String value;

    MilkyStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static MilkyStatus fromValue(String value) {
        for (MilkyStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status: " + value);
    }
}
