package com.shuanglin.framework.enums;

/**
 * 指令类型枚举
 * @deprecated 使用 com.shuanglin.common.enums.CommandType
 */
@Deprecated
public enum CommandType {
    GLOBAL,
    GROUP,
    PRIVATE;

    /**
     * 转换为 common 枚举
     */
    public com.shuanglin.common.enums.CommandType toCommon() {
        return com.shuanglin.common.enums.CommandType.valueOf(this.name());
    }

    /**
     * 从 common 枚举创建
     */
    public static CommandType fromCommon(com.shuanglin.common.enums.CommandType type) {
        return CommandType.valueOf(type.name());
    }
}
