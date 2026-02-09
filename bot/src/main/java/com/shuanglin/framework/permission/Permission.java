package com.shuanglin.framework.permission;

import com.shuanglin.framework.enums.CommandType;
import com.shuanglin.framework.enums.PermissionLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 权限配置模型
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Permission implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 指令名称（唯一标识）
     */
    private String commandName;

    /**
     * 触发前缀
     */
    private String triggerPrefix;

    /**
     * 指令类型
     */
    private CommandType commandType;

    /**
     * 权限级别
     */
    private PermissionLevel level;

    /**
     * 启用状态
     */
    @Builder.Default
    private boolean enabled = true;

    /**
     * 全局黑名单用户ID列表
     */
    private List<String> globalBlacklist;

    /**
     * 全局白名单用户ID列表
     */
    private List<String> globalWhitelist;

    /**
     * 允许使用该指令的角色列表
     */
    private List<String> allowedRoles;

    /**
     * 指令描述
     */
    private String description;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 检查是否在全局黑名单中
     */
    public boolean isInGlobalBlacklist(String userId) {
        return globalBlacklist != null && globalBlacklist.contains(userId);
    }

    /**
     * 检查是否在全局白名单中
     */
    public boolean isInGlobalWhitelist(String userId) {
        return globalWhitelist != null && globalWhitelist.contains(userId);
    }

    /**
     * 检查角色是否允许
     */
    public boolean isRoleAllowed(String roleName) {
        if (allowedRoles == null || allowedRoles.isEmpty()) {
            return true; // 未配置则默认允许
        }
        return allowedRoles.contains(roleName);
    }
}
