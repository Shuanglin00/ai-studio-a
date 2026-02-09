package com.shuanglin.framework.permission;

import com.shuanglin.framework.bus.event.GroupMessageEvent;
import com.shuanglin.framework.bus.event.data.Sender;
import com.shuanglin.framework.command.CommandInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 权限验证器
 * 在指令执行前进行权限校验
 * 优先使用 PermissionManager 进行统一权限校验
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionValidator {

    private final PermissionManager permissionManager;

    /**
     * 执行完整验证
     *
     * @param event   群消息事件
     * @param command 指令信息
     * @return 验证结果
     */
    public ValidationResult validate(GroupMessageEvent event, CommandInfo command) {
        String groupId = event.getGroupId();
        String userId = String.valueOf(event.getUserId());
        String senderRole = getSenderRole(event);

        // 使用 PermissionManager 进行统一权限校验
        PermissionCheckResult result = permissionManager.checkPermission(
                command.getCommandName(),
                groupId,
                userId,
                senderRole
        );

        if (!result.isSuccess()) {
            log.debug("Permission denied for command {}: {}", command.getCommandName(), result.getReason());
            return ValidationResult.fail(result.getReason());
        }

        return ValidationResult.success();
    }

    /**
     * 检查管理员权限（兼容旧接口）
     */
    public Boolean checkAdminPermission(GroupMessageEvent event, String groupId, String userId) {
        String senderRole = getSenderRole(event);

        // 群主或管理员
        if ("owner".equals(senderRole) || "admin".equals(senderRole)) {
            return true;
        }

        // 检查是否是机器人管理员
        return permissionManager.isBotAdmin(userId);
    }

    /**
     * 获取发送者角色
     */
    private String getSenderRole(GroupMessageEvent event) {
        Sender sender = event.getSender();
        if (sender != null && sender.getRole() != null) {
            return sender.getRole().getValue();
        }
        return "member";
    }

    /**
     * 获取发送者 ID
     */
    private String getSenderId(GroupMessageEvent event) {
        Sender sender = event.getSender();
        if (sender != null) {
            return String.valueOf(sender.getUserId());
        }
        return String.valueOf(event.getUserId());
    }
}
