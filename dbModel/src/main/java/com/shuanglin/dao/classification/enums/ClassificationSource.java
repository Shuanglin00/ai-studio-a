package com.shuanglin.dao.classification.enums;

import lombok.Getter;

/**
 * 分类来源枚举
 */
@Getter
public enum ClassificationSource {

    AUTO("auto", "自动分类"),
    MANUAL("manual", "手动标注");

    private final String code;
    private final String description;

    ClassificationSource(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据编码查找来源
     */
    public static ClassificationSource fromCode(String code) {
        if (code == null) {
            throw new IllegalArgumentException("Code cannot be null");
        }
        for (ClassificationSource source : values()) {
            if (source.code.equals(code)) {
                return source;
            }
        }
        throw new IllegalArgumentException("Invalid classification source code: " + code);
    }

    /**
     * 是否为手动标注
     */
    public boolean isManual() {
        return this == MANUAL;
    }

    /**
     * 是否为自动分类
     */
    public boolean isAuto() {
        return this == AUTO;
    }
}
