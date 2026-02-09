package com.shuanglin.framework.listener;

import com.google.gson.Gson;
import com.shuanglin.framework.bus.MessageBus;
import com.shuanglin.framework.bus.event.Event;
import com.shuanglin.framework.bus.event.GroupMessageEvent;
import com.shuanglin.framework.command.CommandInfo;
import com.shuanglin.framework.onebot.builder.GroupMessageBuilder;
import com.shuanglin.framework.permission.PermissionValidator;
import com.shuanglin.framework.permission.ValidationResult;
import com.shuanglin.framework.registry.CommandRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.scheduler.Schedulers;

import java.util.List;

/**
 * 群消息监听器
 * 负责从消息总线接收群消息并分发到对应的指令处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GroupMessageListener {

	private final MessageBus messageBus;
	private final CommandRegistry commandRegistry;
	private final PermissionValidator permissionValidator;
	private final Gson gson = new Gson();

	/**
	 * 订阅消息总线
	 */
	@PostConstruct
	public void subscribe() {
		log.info("GroupMessageListener is subscribing to the message bus...");

		messageBus.getBus()
				// 过滤出群消息事件
				.filter(this::isGroupMessage)
				// 在独立线程中处理
				.publishOn(Schedulers.boundedElastic())
				.subscribe(
						event -> {
							GroupMessageEvent groupEvent = convertToGroupMessage(event);
							if (groupEvent != null) {
								processMessage(groupEvent);
							}
						},
						error -> log.error("Subscription error", error)
				);
	}

	/**
	 * 判断是否是群消息
	 */
	private boolean isGroupMessage(Event event) {
		// Event 基类存储原始数据，检查 message_type
		return "group".equals(event.get("message_type"));
	}

	/**
	 * 将 Event 转换为 GroupMessageEvent
	 */
	private GroupMessageEvent convertToGroupMessage(Event event) {
		if (event instanceof GroupMessageEvent) {
			return (GroupMessageEvent) event;
		}
		// 如果是基类 Event，尝试反序列化
		try {
			String json = gson.toJson(event);
			return gson.fromJson(json, GroupMessageEvent.class);
		} catch (Exception e) {
			log.error("Failed to convert event to GroupMessageEvent", e);
			return null;
		}
	}

	private void processMessage(GroupMessageEvent event) {
		log.info("Received group message: groupId={}, userId={}, rawMessage={}",
				event.getGroupId(), event.getUserId(), event.getRawMessage());

		String messageText = event.getMessageText();
		log.debug("Message text: '{}'", messageText);

		List<CommandInfo> commands = commandRegistry.getAllCommands();
		log.debug("Found {} registered commands", commands.size());

		// 遍历查找匹配的指令
		for (CommandInfo commandInfo : commands) {
			String triggerPrefix = commandInfo.getTriggerPrefix();
			log.debug("Checking command: {} (prefix: '{}')", commandInfo.getCommandName(), triggerPrefix);

			// 检查消息是否匹配触发前缀
			if (messageText == null || !messageText.startsWith(triggerPrefix)) {
				continue;
			}

			log.info("Matched command: {}", commandInfo.getCommandName());

			// 执行权限验证
			ValidationResult validationResult = permissionValidator.validate(event, commandInfo);

			if (!validationResult.isSuccess()) {
				log.warn("Permission denied for {}: {}", commandInfo.getCommandName(), validationResult.getReason());
				if (validationResult.getReason() != null && !validationResult.getReason().equals("角色未启用")) {
					GroupMessageBuilder.forGroup(event.getGroupId())
									.reply(event.getMessageId())
									.text(validationResult.getReason())
									.send();
				}
				continue;
			}

			// 权限验证通过，执行指令
			log.info("Executing command: {}", commandInfo.getCommandName());
			try {
				commandInfo.getMethod().invoke(commandInfo.getBean(), event);
				log.info("Command executed successfully: {}", commandInfo.getCommandName());
			} catch (Exception e) {
				log.error("Error executing command: {}", commandInfo.getCommandName(), e);
				GroupMessageBuilder.forGroup(event.getGroupId())
								.reply(event.getMessageId())
								.text("执行失败: " + e.getMessage())
								.send();
			}

			// 找到匹配的指令后停止遍历
			break;
		}
	}
}
