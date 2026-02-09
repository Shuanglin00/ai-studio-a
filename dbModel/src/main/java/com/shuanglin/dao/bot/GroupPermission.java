package com.shuanglin.dao.bot;

import com.shuanglin.common.enums.CommandType;
import com.shuanglin.dao.base.BaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * 群聊权限配置（MongoDB实体）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Document(collection = "group_permissions")
@CompoundIndexes({
    @CompoundIndex(name = "group_cmd_idx", def = "{'groupId': 1, 'commandName': 1}", unique = true)
})
public class GroupPermission extends BaseEntity {

    @Id
    private String id;

    /**
     * 群号
     */
    @Indexed
    private String groupId;

    /**
     * 指令名称
     */
    @Indexed
    private String commandName;

    /**
     * 指令类型
     */
    @Indexed
    private CommandType commandType;

    /**
     * 是否在该群启用
     */
    @Builder.Default
    private boolean enabled = true;

    /**
     * 群内黑名单用户
     */
    private List<String> blacklist;

    /**
     * 群内白名单用户
     */
    private List<String> whitelist;

    /**
     * 允许使用的角色列表
     */
    private List<String> allowedRoles;

    /**
     * 检查是否在群黑名单中
     */
    public boolean isInBlacklist(String userId) {
        return blacklist != null && blacklist.contains(userId);
    }

    /**
     * 检查是否在群白名单中
     */
    public boolean isInWhitelist(String userId) {
        return whitelist != null && whitelist.contains(userId);
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
