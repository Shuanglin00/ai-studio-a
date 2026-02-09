package com.shuanglin.dao.bot;

import com.shuanglin.common.enums.CommandType;
import com.shuanglin.common.enums.PermissionLevel;
import com.shuanglin.dao.base.BaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

/**
 * 全局权限配置（MongoDB实体）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Document(collection = "global_permissions")
public class GlobalPermission extends BaseEntity {

    @Id
    private String id;

    /**
     * 指令名称（唯一）
     */
    @Indexed(unique = true)
    private String commandName;

    /**
     * 触发前缀
     */
    private String triggerPrefix;

    /**
     * 指令类型（群聊/私聊）
     */
    @Indexed
    private CommandType commandType;

    /**
     * 指令角色要求（用于菜单分类和权限校验）
     * - System: 所有人可用（包括 System, Admin, User）
     * - Admin: 需要群管理权限（包括 Admin, System）
     * - User: 仅普通用户可用
     */
    private String roleType;

    /**
     * 权限级别
     */
    private PermissionLevel level;

    /**
     * 是否启用
     */
    @Builder.Default
    private boolean enabled = true;

    /**
     * 允许使用的角色列表
     */
    private List<String> allowedRoles;

    /**
     * 全局黑名单用户
     */
    private List<String> globalBlacklist;

    /**
     * 全局白名单用户
     */
    private List<String> globalWhitelist;

    /**
     * 扩展配置（灵活存储额外属性）
     */
    private Map<String, Object> extraConfig;

    /**
     * 指令描述
     */
    private String description;

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
            return true;
        }
        return allowedRoles.contains(roleName);
    }
}
