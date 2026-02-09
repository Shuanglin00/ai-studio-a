package com.shuanglin.framework.registry;

import com.shuanglin.dao.bot.Command;
import com.shuanglin.dao.bot.CommandRepository;
import com.shuanglin.framework.annotation.GroupMessageHandler;
import com.shuanglin.framework.command.CommandInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommandRegistry {

	private final ApplicationContext applicationContext;
	private final CommandRepository commandRepository;
	private final ApplicationEventPublisher eventPublisher;

	@Getter
	private final List<CommandInfo> commandInfoList = new ArrayList<>();

	private volatile boolean initialized = false;

	// 监听 Spring 容器刷新完成事件，此时所有 Bean 都已初始化
	@EventListener(ContextRefreshedEvent.class)
	public void onApplicationEvent(ContextRefreshedEvent event) {
		// 防止重复初始化（Root context 和 Servlet context 都会触发）
		if (initialized) {
			log.debug("CommandRegistry already initialized, skipping");
			return;
		}

		// 只处理根上下文
		if (event.getApplicationContext().getParent() != null) {
			log.debug("Skipping child context refresh");
			return;
		}

		initialized = true;
		log.info("Framework startup: Scanning for message handlers...");
		scanGroupMessageHandlers();
		syncCommandsToDatabase();
		log.info("Framework startup: Scan complete. Found {} group message handlers.", commandInfoList.size());

		// 打印所有已注册的指令
		for (CommandInfo cmd : commandInfoList) {
			log.info("Registered command: {} (trigger: '{}', role: {})",
					cmd.getCommandName(), cmd.getTriggerPrefix(), cmd.getRole());
		}

		// 发布命令注册完成事件，让权限系统监听并初始化
		publishCommandRegistryReadyEvent();
	}

	private void publishCommandRegistryReadyEvent() {
		eventPublisher.publishEvent(new CommandRegistryReadyEvent(this, commandInfoList));
		log.info("Published CommandRegistryReadyEvent for {} commands", commandInfoList.size());
	}

	private void scanGroupMessageHandlers() {
		// 获取所有被 @GroupMessageHandler 注解的方法所在的 Bean
		Map<String, Object> beans = applicationContext.getBeansWithAnnotation(Component.class); // 扫描所有组件
		for (Object bean : beans.values()) {
			// Spring 会创建代理类，要获取原始类来检查方法
			Class<?> targetClass = AopUtils.getTargetClass(bean);
			for (Method method : targetClass.getDeclaredMethods()) {
				if (method.isAnnotationPresent(GroupMessageHandler.class)) {
					GroupMessageHandler annotation = method.getAnnotation(GroupMessageHandler.class);
					// 确保方法只有一个参数，即 payload
					if (method.getParameterCount() != 1) {
						log.error("Method {} annotated with @GroupMessageHandler must have exactly one parameter (the payload).", method.getName());
						continue;
					}
					commandInfoList.add(buildCommandInfo(annotation, method, bean));
					log.info("Registered handler: {}.{}", targetClass.getSimpleName(), method.getName());
				}
			}
		}
	}

	/**
	 * 将扫描到的指令同步到数据库
	 * 先清理已注册的指令，再重新插入（保证数据一致性）
	 */
	private void syncCommandsToDatabase() {
		log.info("Syncing commands to database...");

		// 1. 先删除所有已存在的指令（使用 deleteByCommandName 清理重复数据）
		for (CommandInfo existingCmd : commandInfoList) {
			commandRepository.deleteByCommandName(existingCmd.getCommandName());
			log.debug("Deleted existing command: {}", existingCmd.getCommandName());
		}

		// 2. 重新插入所有指令
		for (CommandInfo cmdInfo : commandInfoList) {
			Command command = Command.builder()
					.commandName(cmdInfo.getCommandName())
					.triggerPrefix(cmdInfo.getTriggerPrefix())
					.role(cmdInfo.getRole().name())
					.description(cmdInfo.getDescription())
					.createTime(LocalDateTime.now())
					.updateTime(LocalDateTime.now())
					.build();

			commandRepository.save(command);
			log.debug("Saved command to database: {}", cmdInfo.getCommandName());
		}

		log.info("Commands synced to database. Total: {}", commandInfoList.size());
	}

	/**
	 * 构建CommandInfo
	 */
	private CommandInfo buildCommandInfo(GroupMessageHandler annotation, Method method, Object bean) {
		// 提取指令名称：驼峰转下划线
		String commandName = convertMethodNameToCommandName(method.getName());

		return CommandInfo.builder()
						.commandName(commandName)
						.triggerPrefix(annotation.triggerPrefix())
						.role(annotation.role())
						.requireAdmin(annotation.requireAdmin())
						.description(annotation.description())
						.bean(bean)
						.method(method)
						.condition(annotation.condition())
						.build();
	}

	/**
	 * 驼峰转下划线
	 * 例如：chatCommand -> chat_command
	 */
	private String convertMethodNameToCommandName(String methodName) {
		return methodName.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
	}

	/**
	 * 获取所有已注册指令
	 */
	public List<CommandInfo> getAllCommands() {
		return new ArrayList<>(commandInfoList);
	}

	/**
	 * 根据触发前缀查找指令
	 */
	public CommandInfo getCommandByPrefix(String prefix) {
		return commandInfoList.stream()
						.filter(cmd -> cmd.getTriggerPrefix().equals(prefix))
						.findFirst()
						.orElse(null);
	}

	/**
	 * 获取指定角色的指令集
	 */
	public List<CommandInfo> getCommandsByRole(String roleName) {
		return commandInfoList.stream()
						.filter(cmd -> cmd.getRole().name().equals(roleName))
						.collect(Collectors.toList());
	}

	/**
	 * 根据指令名称查询
	 */
	public CommandInfo getCommandByName(String commandName) {
		return commandInfoList.stream()
						.filter(cmd -> cmd.getCommandName().equals(commandName))
						.findFirst()
						.orElse(null);
	}

	/**
	 * 获取所有指令按角色分组
	 */
	public Map<String, List<CommandInfo>> getAllCommandsGroupedByRole() {
		Map<String, List<CommandInfo>> grouped = new HashMap<>();
		for (CommandInfo cmd : commandInfoList) {
			grouped.computeIfAbsent(cmd.getRole().name(), k -> new ArrayList<>()).add(cmd);
		}
		return grouped;
	}

	/**
	 * 命令注册完成事件
	 */
	public static class CommandRegistryReadyEvent {
		private final CommandRegistry source;
		private final List<CommandInfo> commands;

		public CommandRegistryReadyEvent(CommandRegistry source, List<CommandInfo> commands) {
			this.source = source;
			this.commands = commands;
		}

		public CommandRegistry getSource() {
			return source;
		}

		public List<CommandInfo> getCommands() {
			return commands;
		}
	}
}