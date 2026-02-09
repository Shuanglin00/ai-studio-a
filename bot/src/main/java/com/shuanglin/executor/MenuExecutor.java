package com.shuanglin.executor;

import com.shuanglin.framework.annotation.GroupMessageHandler;
import com.shuanglin.framework.bus.event.GroupMessageEvent;
import com.shuanglin.framework.command.CommandInfo;
import com.shuanglin.framework.enums.RoleType;
import com.shuanglin.framework.onebot.builder.GroupMessageBuilder;
import com.shuanglin.framework.registry.CommandRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 菜单执行器
 * 提供指令菜单查询功能
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MenuExecutor {

  private final CommandRegistry commandRegistry;

  /**
   * 显示指令菜单
   */
  @GroupMessageHandler(triggerPrefix = "#菜单", role = RoleType.User, description = "查看指令菜单")
  public void showMenu(GroupMessageEvent event) {
    log.debug("Show menu");
    String groupId = event.getGroupId();

    // 获取所有指令按角色分组
    Map<String, List<CommandInfo>> commandsByRole = commandRegistry.getAllCommandsGroupedByRole();

    // 构建菜单消息
    StringBuilder menu = new StringBuilder();
    menu.append("═══════ 指令菜单 ═══════\n\n");

    for (Map.Entry<String, List<CommandInfo>> entry : commandsByRole.entrySet()) {
      String roleName = entry.getKey();
      List<CommandInfo> commands = entry.getValue();

      menu.append("【").append(roleName).append("】\n");
      for (CommandInfo cmd : commands) {
        menu.append("  ").append(cmd.getTriggerPrefix());
        if (!cmd.getDescription().isEmpty()) {
          menu.append(" - ").append(cmd.getDescription());
        }
        if (cmd.getRequireAdmin()) {
          menu.append(" 🔒");
        }
        menu.append("\n");
      }
      menu.append("\n");
    }

    menu.append("━━━━━━━━━━━━━━━━━━━\n");
    menu.append("🔒 需要管理员权限\n");
    menu.append("发送 #角色列表 查看所有可用角色");

    // 发送菜单
    GroupMessageBuilder.forGroup(groupId)
            .reply(event.getMessageId())
            .text(menu.toString())
            .send();

    log.info("Displayed menu to group {}", groupId);
  }

  /**
   * 显示帮助信息
   */
  @GroupMessageHandler(triggerPrefix = "#help", role = RoleType.System, description = "查看帮助")
  public void showHelp(GroupMessageEvent event) {
    showMenu(event);
  }
}
