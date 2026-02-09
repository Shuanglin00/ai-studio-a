package com.shuanglin.executor;

import com.shuanglin.dao.bot.BotRole;
import com.shuanglin.framework.annotation.GroupMessageHandler;
import com.shuanglin.framework.bus.event.GroupMessageEvent;
import com.shuanglin.framework.config.GroupConfigManager;
import com.shuanglin.framework.enums.RoleType;
import com.shuanglin.framework.onebot.builder.GroupMessageBuilder;
import com.shuanglin.framework.role.RoleManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 角色管理执行器
 * 提供角色管理相关指令
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RoleManagementExecutor {

    private final RoleManager roleManager;
    private final GroupConfigManager groupConfigManager;

    /**
     * 启用角色
     */
    @GroupMessageHandler(triggerPrefix = "#启用角色", role = RoleType.System, description = "为当前群聊启用指定角色", requireAdmin = true)
    public void enableRole(GroupMessageEvent event) {
        String groupId = event.getGroupId();
        String message = event.getRawMessage();

        // 提取角色名称
        String roleName = extractRoleName(message, "#启用角色");
        if (roleName == null) {
            GroupMessageBuilder.forGroup(groupId)
                    .reply(event.getMessageId())
                    .text("❌ 参数错误\n使用方式：#启用角色 角色名")
                    .send();
            return;
        }

        // 检查角色是否存在
        BotRole role = roleManager.getRoleByName(roleName);
        if (role == null) {
            GroupMessageBuilder.forGroup(groupId)
                    .reply(event.getMessageId())
                    .text("❌ 角色不存在：" + roleName)
                    .send();
            return;
        }

        // 启用角色
        groupConfigManager.enableRole(groupId, roleName);

        GroupMessageBuilder.forGroup(groupId)
                .reply(event.getMessageId())
                .text("✅ 已启用角色：" + roleName)
                .send();

        log.info("Enabled role {} for group {}", roleName, groupId);
    }

    /**
     * 禁用角色
     */
    @GroupMessageHandler(triggerPrefix = "#禁用角色", role = RoleType.System, description = "禁用指定角色", requireAdmin = true)
    public void disableRole(GroupMessageEvent event) {
        String groupId = event.getGroupId();
        String message = event.getRawMessage();

        String roleName = extractRoleName(message, "#禁用角色");
        if (roleName == null) {
            GroupMessageBuilder.forGroup(groupId)
                    .reply(event.getMessageId())
                    .text("❌ 参数错误\n使用方式：#禁用角色 角色名")
                    .send();
            return;
        }

        groupConfigManager.disableRole(groupId, roleName);

        GroupMessageBuilder.forGroup(groupId)
                .reply(event.getMessageId())
                .text("✅ 已禁用角色：" + roleName)
                .send();

        log.info("Disabled role {} for group {}", roleName, groupId);
    }

    /**
     * 查看角色列表
     */
    @GroupMessageHandler(triggerPrefix = "#角色列表", role = RoleType.System, description = "查看所有可用角色")
    public void listRoles(GroupMessageEvent event) {
        String groupId = event.getGroupId();

        List<BotRole> roles = roleManager.getAllRoles();

        StringBuilder sb = new StringBuilder();
        sb.append("═══════ 角色列表 ═══════\n\n");

        for (BotRole role : roles) {
            sb.append("【").append(role.getRoleName()).append("】");
            if (role.getIsActive()) {
                sb.append(" ✅");
            } else {
                sb.append(" ❌");
            }
            sb.append("\n");

            if (role.getDescription() != null && !role.getDescription().isEmpty()) {
                sb.append("  描述：").append(role.getDescription()).append("\n");
            }

            if (role.getCommandNames() != null && !role.getCommandNames().isEmpty()) {
                sb.append("  指令：").append(String.join(", ", role.getCommandNames())).append("\n");
            }
            sb.append("\n");
        }

        GroupMessageBuilder.forGroup(groupId)
                .reply(event.getMessageId())
                .text(sb.toString())
                .send();
    }

    /**
     * 提取角色名称
     */
    private String extractRoleName(String message, String prefix) {
        String pattern = prefix + "\\s+(.+)";
        Matcher matcher = Pattern.compile(pattern).matcher(message);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }
}
