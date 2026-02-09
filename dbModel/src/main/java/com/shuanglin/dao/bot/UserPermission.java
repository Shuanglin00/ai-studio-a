package com.shuanglin.dao.bot;

import com.shuanglin.dao.base.BaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * 用户权限配置（MongoDB实体，跨群生效）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Document(collection = "user_permissions")
public class UserPermission extends BaseEntity {

    @Id
    private String id;

    /**
     * 用户ID
     */
    @Indexed(unique = true)
    private String userId;

    /**
     * 允许使用的指令列表
     */
    private List<String> allowedCommands;

    /**
     * 禁止使用的指令列表
     */
    private List<String> deniedCommands;

    /**
     * 是否是机器人管理员
     */
    @Builder.Default
    private boolean botAdmin = false;

    /**
     * 检查是否允许使用指令
     */
    public boolean isCommandAllowed(String commandName) {
        // 黑名单优先
        if (deniedCommands != null && deniedCommands.contains(commandName)) {
            return false;
        }
        // 白名单检查
        if (allowedCommands != null && !allowedCommands.isEmpty()) {
            return allowedCommands.contains(commandName);
        }
        return true; // 未配置则默认允许
    }
}
