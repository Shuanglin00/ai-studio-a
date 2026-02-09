package com.shuanglin.common.enums;

/**
 * 权限级别枚举
 */
public enum PermissionLevel {
    /**
     * 任何人可用（最低级别）
     */
    PUBLIC,

    /**
     * 需要登录用户
     */
    USER,

    /**
     * 需要群聊管理员
     */
    GROUP_ADMIN,

    /**
     * 需要群主
     */
    GROUP_OWNER,

    /**
     * 需要机器人管理员
     */
    BOT_ADMIN,

    /**
     * 仅限特定用户（白名单）
     */
    WHITELIST,

    /**
     * 黑名单禁止（最高级别，优先级最高）
     */
    BLACKLIST
}
