package com.shuanglin.framework.onebot.segment;

import lombok.Getter;

/**
 * 商城表情消息段
 */
@Getter
public class MfaceSegment extends MessageSegment {

    public MfaceSegment(String emojiPackageId, String emojiId, String key) {
        this.type = "mface";
        this.data.put("emoji_package_id", emojiPackageId);
        this.data.put("emoji_id", emojiId);
        this.data.put("key", key);
    }

    public void setSummary(String summary) {
        this.data.put("summary", summary);
    }

    public void setUrl(String url) {
        this.data.put("url", url);
    }

    @Override
    public void validate() {
        if (data.get("emoji_package_id") == null) {
            throw new IllegalArgumentException("Emoji package ID cannot be null");
        }
        if (data.get("emoji_id") == null) {
            throw new IllegalArgumentException("Emoji ID cannot be null");
        }
        if (data.get("key") == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
    }
}
