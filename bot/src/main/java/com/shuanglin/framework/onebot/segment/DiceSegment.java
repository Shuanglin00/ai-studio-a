package com.shuanglin.framework.onebot.segment;

import lombok.Getter;

/**
 * 骰子消息段
 */
@Getter
public class DiceSegment extends MessageSegment {

    public DiceSegment(int result) {
        this.type = "dice";
        this.data.put("result", result);
    }

    @Override
    public void validate() {
        Object result = data.get("result");
        if (result == null) {
            throw new IllegalArgumentException("Dice result cannot be null");
        }
        int value = ((Number) result).intValue();
        if (value < 1 || value > 6) {
            throw new IllegalArgumentException("Dice result must be between 1 and 6");
        }
    }
}
