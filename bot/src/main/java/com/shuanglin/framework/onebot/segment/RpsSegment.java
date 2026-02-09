package com.shuanglin.framework.onebot.segment;

import lombok.Getter;

/**
 * 石头剪刀布消息段
 */
@Getter
public class RpsSegment extends MessageSegment {

    /**
     * 猜拳结果常量
     */
    public static final int ROCK = 1;
    public static final int SCISSORS = 2;
    public static final int PAPER = 3;

    public RpsSegment(int result) {
        this.type = "rps";
        this.data.put("result", result);
    }

    @Override
    public void validate() {
        Object result = data.get("result");
        if (result == null) {
            throw new IllegalArgumentException("RPS result cannot be null");
        }
        int value = ((Number) result).intValue();
        if (value < 1 || value > 3) {
            throw new IllegalArgumentException("RPS result must be 1, 2, or 3 (rock, scissors, paper)");
        }
    }
}
