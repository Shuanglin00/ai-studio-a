package com.shuanglin.framework.permission;

import com.shuanglin.common.enums.CommandType;
import com.shuanglin.framework.command.CommandInfo;
import com.shuanglin.framework.registry.CommandRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 指令菜单服务
 * 提供指令菜单的生成和管理功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MenuService {

    private final CommandRegistry commandRegistry;
    private final PermissionManager permissionManager;

    /**
     * 获取所有指令菜单
     */
    public CommandMenu getFullMenu() {
        CommandMenu menu = new CommandMenu();
        menu.setGlobalCommands(getGlobalCommands());
        menu.setGroupCommands(getGroupCommands());
        menu.setPrivateCommands(getPrivateCommands());
        return menu;
    }

    /**
     * 获取指定群组的可用指令菜单
     */
    public CommandMenu getAvailableMenu(String groupId, String userId, String senderRole) {
        CommandMenu menu = new CommandMenu();

        List<String> availableCmds = permissionManager.getAvailableCommands(groupId, userId, senderRole);
        Set<String> availableSet = new HashSet<>(availableCmds);

        menu.setGlobalCommands(filterCommands(getGlobalCommands(), availableSet));
        menu.setGroupCommands(filterCommands(getGroupCommands(), availableSet));
        menu.setPrivateCommands(filterCommands(getPrivateCommands(), availableSet));

        return menu;
    }

    /**
     * 获取全局指令列表
     */
    public List<CommandItem> getGlobalCommands() {
        return commandRegistry.getAllCommands().stream()
                .filter(cmd -> {
                    CommandType type = getCommandType(cmd);
                    return type == CommandType.GLOBAL;
                })
                .map(this::toCommandItem)
                .collect(Collectors.toList());
    }

    /**
     * 获取群聊指令列表
     */
    public List<CommandItem> getGroupCommands() {
        return commandRegistry.getAllCommands().stream()
                .filter(cmd -> {
                    CommandType type = getCommandType(cmd);
                    return type == CommandType.GROUP;
                })
                .map(this::toCommandItem)
                .collect(Collectors.toList());
    }

    /**
     * 获取私聊指令列表
     */
    public List<CommandItem> getPrivateCommands() {
        return commandRegistry.getAllCommands().stream()
                .filter(cmd -> {
                    CommandType type = getCommandType(cmd);
                    return type == CommandType.PRIVATE;
                })
                .map(this::toCommandItem)
                .collect(Collectors.toList());
    }

    /**
     * 获取指令详情
     */
    public Optional<CommandItem> getCommandDetail(String commandName) {
        CommandInfo cmd = commandRegistry.getCommandByName(commandName);
        return cmd != null ? Optional.of(toCommandItem(cmd)) : Optional.empty();
    }

    /**
     * 根据触发前缀查找指令
     */
    public Optional<CommandItem> getCommandByPrefix(String prefix) {
        CommandInfo cmd = commandRegistry.getCommandByPrefix(prefix);
        return cmd != null ? Optional.of(toCommandItem(cmd)) : Optional.empty();
    }

    /**
     * 过滤出可用的指令
     */
    private List<CommandItem> filterCommands(List<CommandItem> commands, Set<String> availableSet) {
        return commands.stream()
                .filter(cmd -> availableSet.contains(cmd.getName()))
                .collect(Collectors.toList());
    }

    /**
     * 获取指令类型
     */
    private CommandType getCommandType(CommandInfo cmd) {
        // 优先从 PermissionManager 获取
        return permissionManager.getGlobalPermission(cmd.getCommandName())
                .map(com.shuanglin.dao.bot.GlobalPermission::getCommandType)
                .orElseGet(() -> detectCommandType(cmd));
    }

    /**
     * 从 CommandInfo 推断指令类型
     */
    private CommandType detectCommandType(CommandInfo cmd) {
        String prefix = cmd.getTriggerPrefix().toLowerCase();
        if (prefix.startsWith("!") || prefix.startsWith("/")) {
            return CommandType.GLOBAL;
        }
        return CommandType.GROUP;
    }

    /**
     * 转换为 CommandItem
     */
    private CommandItem toCommandItem(CommandInfo cmd) {
        return CommandItem.builder()
                .name(cmd.getCommandName())
                .triggerPrefix(cmd.getTriggerPrefix())
                .description(cmd.getDescription())
                .role(cmd.getRole().name())
                .requireAdmin(cmd.getRequireAdmin())
                .build();
    }

    /**
     * 生成菜单文本
     */
    public String buildMenuText(CommandMenu menu, String groupId, String userId, String senderRole) {
        CommandMenu availableMenu = getAvailableMenu(groupId, userId, senderRole);
        StringBuilder sb = new StringBuilder();

        sb.append("┌─ 指令菜单 ─\n");

        if (!availableMenu.getGlobalCommands().isEmpty()) {
            sb.append("├─ 全局指令\n");
            for (CommandItem cmd : availableMenu.getGlobalCommands()) {
                sb.append("│  ").append(cmd.getTriggerPrefix())
                  .append(" - ").append(cmd.getDescription()).append("\n");
            }
        }

        if (!availableMenu.getGroupCommands().isEmpty()) {
            sb.append("├─ 群聊指令\n");
            for (CommandItem cmd : availableMenu.getGroupCommands()) {
                String adminMark = cmd.isRequireAdmin() ? " [管理员]" : "";
                sb.append("│  ").append(cmd.getTriggerPrefix())
                  .append(" - ").append(cmd.getDescription())
                  .append(adminMark).append("\n");
            }
        }

        if (!availableMenu.getPrivateCommands().isEmpty()) {
            sb.append("├─ 私聊指令\n");
            for (CommandItem cmd : availableMenu.getPrivateCommands()) {
                sb.append("│  ").append(cmd.getTriggerPrefix())
                  .append(" - ").append(cmd.getDescription()).append("\n");
            }
        }

        sb.append("└───────────");
        return sb.toString();
    }
}
