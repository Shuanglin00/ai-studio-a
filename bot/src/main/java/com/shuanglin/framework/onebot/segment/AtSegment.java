package com.shuanglin.framework.onebot.segment;

/**
 * @消息段
 */
public class AtSegment extends MessageSegment {

    public AtSegment(String qq) {
        this.type = "at";
        this.data.put("qq", qq);
    }

    /**
     * @全体成员
     */
    public static AtSegment atAll() {
        return new AtSegment("all");
    }

    @Override
    public void validate() {
        if (data.get("qq") == null) {
            throw new IllegalArgumentException("QQ number cannot be null");
        }
    }
}
