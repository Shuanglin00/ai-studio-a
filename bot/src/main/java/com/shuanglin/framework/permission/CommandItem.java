package com.shuanglin.framework.permission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 指令项
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommandItem {

    /**
     * 指令名称
     */
    private String name;

    /**
     * 触发前缀
     */
    private String triggerPrefix;

    /**
     * 指令描述
     */
    private String description;

    /**
     * 所属角色
     */
    private String role;

    /**
     * 是否需要管理员权限
     */
    @Builder.Default
    private boolean requireAdmin = false;

    /**
     * 权限级别
     */
    private String permissionLevel;
}
