package com.shuanglin.dao.classification.enums;

import lombok.Getter;

/**
 * 消息收集批次状态枚举
 */
@Getter
public enum BatchStatus {

    COLLECTING("collecting", "收集中"),
    COMPLETED("completed", "收集完成"),
    CLASSIFIED("classified", "已分类"),
    FAILED("failed", "处理失败");

    private final String code;
    private final String description;

    BatchStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据编码查找状态
     */
    public static BatchStatus fromCode(String code) {
        if (code == null) {
            throw new IllegalArgumentException("Code cannot be null");
        }
        for (BatchStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid batch status code: " + code);
    }

    /**
     * 是否为进行中状态
     */
    public boolean isInProgress() {
        return this == COLLECTING;
    }

    /**
     * 是否为终止状态
     */
    public boolean isTerminal() {
        return this == COMPLETED || this == CLASSIFIED || this == FAILED;
    }
}
