package com.shuanglin.executor;

import com.shuanglin.dao.bot.GroupConfiguration;
import com.shuanglin.framework.annotation.GroupMessageHandler;
import com.shuanglin.framework.bus.event.GroupMessageEvent;
import com.shuanglin.framework.config.GroupConfigManager;
import com.shuanglin.framework.enums.RoleType;
import com.shuanglin.framework.onebot.builder.GroupMessageBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 管理员管理执行器
 * 提供机器人管理员管理功能
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminManagementExecutor {

    private final GroupConfigManager groupConfigManager;

    /**
     * 添加管理员
     */
    @GroupMessageHandler(triggerPrefix = "#添加管理员", role = RoleType.System, description = "添加机器人管理员", requireAdmin = true)
    public void addAdmin(GroupMessageEvent event) {
        String groupId = event.getGroupId();
        String message = event.getRawMessage();

        // 提取QQ号（支持@消息）
        String userId = extractQQ(message);
        if (userId == null) {
            GroupMessageBuilder.forGroup(groupId)
                    .reply(event.getMessageId())
                    .text("❌ 参数错误\n使用方式：#添加管理员 @用户 或 #添加管理员 QQ号")
                    .send();
            return;
        }

        groupConfigManager.addAdmin(groupId, userId);

        GroupMessageBuilder.forGroup(groupId)
                .reply(event.getMessageId())
                .at(userId)
                .text(" 已被添加为机器人管理员")
                .send();

        log.info("Added admin {} to group {}", userId, groupId);
    }

    /**
     * 移除管理员
     */
    @GroupMessageHandler(triggerPrefix = "#移除管理员", role = RoleType.System, description = "移除机器人管理员", requireAdmin = true)
    public void removeAdmin(GroupMessageEvent event) {
        String groupId = event.getGroupId();
        String message = event.getRawMessage();

        String userId = extractQQ(message);
        if (userId == null) {
            GroupMessageBuilder.forGroup(groupId)
                    .reply(event.getMessageId())
                    .text("❌ 参数错误\n使用方式：#移除管理员 @用户 或 #移除管理员 QQ号")
                    .send();
            return;
        }

        groupConfigManager.removeAdmin(groupId, userId);

        GroupMessageBuilder.forGroup(groupId)
                .reply(event.getMessageId())
                .text("✅ 已移除管理员：" + userId)
                .send();

        log.info("Removed admin {} from group {}", userId, groupId);
    }

    /**
     * 管理员列表
     */
    @GroupMessageHandler(triggerPrefix = "#管理员列表", role = RoleType.System, description = "查看机器人管理员列表")
    public void listAdmins(GroupMessageEvent event) {
        String groupId = event.getGroupId();

        GroupConfiguration config = groupConfigManager.getGroupConfig(groupId);
        List<String> admins = config.getAdmins();

        StringBuilder sb = new StringBuilder();
        sb.append("═══════ 管理员列表 ═══════\n\n");

        if (admins == null || admins.isEmpty()) {
            sb.append("暂无机器人管理员");
        } else {
            for (int i = 0; i < admins.size(); i++) {
                sb.append((i + 1)).append(". ").append(admins.get(i)).append("\n");
            }
        }

        GroupMessageBuilder.forGroup(groupId)
                .reply(event.getMessageId())
                .text(sb.toString())
                .send();
    }

    /**
     * 提取QQ号
     * 支持两种格式：
     * 1. @消息: [CQ:at,qq=123456]
     * 2. 纯数字: #添加管理员 123456
     */
    private String extractQQ(String message) {
        // 尝试提取@消息中的QQ号
        Pattern atPattern = Pattern.compile("\\[CQ:at,qq=(\\d+)\\]");
        Matcher atMatcher = atPattern.matcher(message);
        if (atMatcher.find()) {
            return atMatcher.group(1);
        }

        // 尝试提取纯数字QQ号
        Pattern qqPattern = Pattern.compile("#(?:添加|移除)管理员\\s+(\\d+)");
        Matcher qqMatcher = qqPattern.matcher(message);
        if (qqMatcher.find()) {
            return qqMatcher.group(1);
        }

        return null;
    }
}
