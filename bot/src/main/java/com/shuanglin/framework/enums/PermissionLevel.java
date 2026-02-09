package com.shuanglin.framework.enums;

/**
 * 权限级别枚举
 * @deprecated 使用 com.shuanglin.common.enums.PermissionLevel
 */
@Deprecated
public enum PermissionLevel {
    PUBLIC,
    USER,
    GROUP_ADMIN,
    GROUP_OWNER,
    BOT_ADMIN,
    WHITELIST,
    BLACKLIST;

    /**
     * 转换为 common 枚举
     */
    public com.shuanglin.common.enums.PermissionLevel toCommon() {
        return com.shuanglin.common.enums.PermissionLevel.valueOf(this.name());
    }

    /**
     * 从 common 枚举创建
     */
    public static PermissionLevel fromCommon(com.shuanglin.common.enums.PermissionLevel level) {
        return PermissionLevel.valueOf(level.name());
    }
}
