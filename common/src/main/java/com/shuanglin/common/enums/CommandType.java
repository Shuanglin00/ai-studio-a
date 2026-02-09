package com.shuanglin.common.enums;

/**
 * 指令类型枚举
 */
public enum CommandType {
    /**
     * 全局指令 - 所有用户在任何地方都可使用
     */
    GLOBAL,

    /**
     * 群聊指令 - 仅在群聊中可用
     */
    GROUP,

    /**
     * 私聊指令 - 仅在私聊中可用
     */
    PRIVATE
}
