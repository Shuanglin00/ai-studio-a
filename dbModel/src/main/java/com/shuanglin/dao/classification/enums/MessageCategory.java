package com.shuanglin.dao.classification.enums;

import lombok.Getter;

/**
 * 消息分类枚举
 */
@Getter
public enum MessageCategory {

    MEME("meme", "弔图", false),
    HELL_JOKE("hell-joke", "地狱笑话", true),
    REGIONAL_BLACK("regional-black", "地域黑", true),
    POLITICAL("political", "政治敏感", true),
    NSFW("nsfw", "NSFW", true),
    NORMAL("normal", "正常", false),
    SPAM("spam", "刷屏", false),
    AD("ad", "广告", false),
    OTHER("other", "其他", false);

    private final String code;
    private final String name;
    private final boolean sensitive;

    MessageCategory(String code, String name, boolean sensitive) {
        this.code = code;
        this.name = name;
        this.sensitive = sensitive;
    }

    /**
     * 根据编码查找分类
     */
    public static MessageCategory fromCode(String code) {
        if (code == null) {
            throw new IllegalArgumentException("Code cannot be null");
        }
        for (MessageCategory category : values()) {
            if (category.code.equals(code)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Invalid category code: " + code);
    }

    /**
     * 是否为敏感分类
     */
    public boolean isSensitive() {
        return sensitive;
    }
}
