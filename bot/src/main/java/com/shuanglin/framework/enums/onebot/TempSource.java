package com.shuanglin.framework.enums.onebot;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 临时聊天来源枚举
 */
@Getter
@AllArgsConstructor
public enum TempSource {
    GROUP_CHAT(0),
    NORMAL(1),
    ANONYMOUS(2),
    UNKNOWN_3(3),
    UNKNOWN_4(4),
    UNKNOWN_6(6),
    UNKNOWN_7(7),
    UNKNOWN_8(8),
    UNKNOWN_9(9);

    private final int value;
}
