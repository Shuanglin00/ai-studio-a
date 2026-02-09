package com.shuanglin.framework.permission;

import com.shuanglin.common.enums.CommandType;
import com.shuanglin.common.enums.PermissionLevel;
import com.shuanglin.dao.bot.*;
import com.shuanglin.framework.command.CommandInfo;
import com.shuanglin.framework.enums.RoleType;
import com.shuanglin.framework.registry.CommandRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 权限管理器
 * 核心职责：
 * 1. 启动时从 MongoDB 加载全局权限到 Redis
 * 2. 权限校验时自动初始化群聊/用户权限（懒加载）
 * 3. 提供权限校验接口
 * 4. 变更时遵循 Cache-Aside 模式
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionManager {

    private final GlobalPermissionRepository globalPermRepository;
    private final GroupPermissionRepository groupPermRepository;
    private final UserPermissionRepository userPermRepository;
    private final RedisPermissionStore redisStore;

    // ==================== 启动加载 ====================

    /**
     * 监听命令注册完成事件，初始化权限系统
     */
    @EventListener
    public void onCommandRegistryReady(CommandRegistry.CommandRegistryReadyEvent event) {
        log.info("Permission Manager: Received CommandRegistryReadyEvent with {} commands",
                event.getCommands().size());

        try {
            // 加载已有全局权限
            loadGlobalPermissions();
            // 同步命令到权限系统
            syncCommandTypes(event.getCommands());
            log.info("Permission Manager: Initialization complete");
        } catch (Exception e) {
            log.error("Failed to initialize permissions", e);
        }
    }

    /**
     * 加载全局权限
     */
    private void loadGlobalPermissions() {
        List<GlobalPermission> permissions = globalPermRepository.findAll();
        for (GlobalPermission perm : permissions) {
            redisStore.saveGlobalPermission(perm);
        }
        log.info("Loaded {} global permissions to Redis", permissions.size());
    }

    /**
     * 同步指令类型（从代码注册的命令）
     * 如果全局权限不存在，则创建并保存到 MongoDB 和 Redis
     * 如果是新指令，自动为所有群聊创建默认权限
     */
    public void syncCommandTypes(List<CommandInfo> commands) {
        int created = 0;
        int loaded = 0;
        int skipped = 0;

        for (CommandInfo cmd : commands) {
            String cmdName = cmd.getCommandName();

            // 检查 Redis 是否已存在
            if (redisStore.getGlobalPermission(cmdName).isPresent()) {
                log.debug("Skipped (in Redis): {}", cmdName);
                skipped++;
                continue;
            }

            // 检查 MongoDB 是否已存在
            Optional<GlobalPermission> existing = globalPermRepository.findByCommandName(cmdName);
            if (existing.isPresent()) {
                // MongoDB 存在，保存到 Redis
                redisStore.saveGlobalPermission(existing.get());
                log.debug("Loaded from MongoDB: {}", cmdName);
                loaded++;
            } else {
                // 都不存在，创建默认权限并保存到 MongoDB 和 Redis
                GlobalPermission defaultPerm = createDefaultGlobalPermission(cmd);
                globalPermRepository.save(defaultPerm);
                redisStore.saveGlobalPermission(defaultPerm);
                log.info("Created permission for command: {}", cmdName);
                created++;

                // 为新指令创建群聊权限
                initPermissionForAllGroups(cmdName);
            }
        }

        log.info("Permission sync complete: created={}, loaded={}, skipped={}", created, loaded, skipped);
    }

    /**
     * 为所有群聊初始化指定指令的权限（用于新指令同步）
     * 只为已经存在群聊权限记录的群聊创建，如果某群聊从未配置过任何权限则跳过
     */
    private void initPermissionForAllGroups(String commandName) {
        // 获取所有已有群聊权限记录的群ID
        List<String> existingGroupIds = groupPermRepository.findAll().stream()
                .map(GroupPermission::getGroupId)
                .distinct()
                .toList();

        if (existingGroupIds.isEmpty()) {
            log.debug("No existing groups found for new command: {}", commandName);
            return;
        }

        for (String groupId : existingGroupIds) {
            // 检查是否已存在该指令的权限
            if (!groupPermRepository.existsByGroupIdAndCommandName(groupId, commandName)) {
                GroupPermission defaultPerm = createDefaultGroupPermission(groupId, commandName);
                saveGroupPermission(defaultPerm);
                log.debug("Created permission for new command in group: {}, cmd={}", groupId, commandName);
            }
        }
        log.info("Initialized permission for new command '{}' in {} groups", commandName, existingGroupIds.size());
    }

    /**
     * 创建默认全局权限
     */
    private GlobalPermission createDefaultGlobalPermission(CommandInfo cmd) {
        return GlobalPermission.builder()
                .commandName(cmd.getCommandName())
                .triggerPrefix(cmd.getTriggerPrefix())
                .commandType(detectCommandType(cmd))
                .roleType(cmd.getRole().name())  // 使用指令注册时的 RoleType
                .level(PermissionLevel.PUBLIC)
                .enabled(true)
                // allowedRoles 为空或 null 表示使用 roleType 进行角色检查
                .allowedRoles(null)
                .description(cmd.getDescription())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }

    /**
     * 从 CommandInfo 推断指令类型
     */
    private CommandType detectCommandType(CommandInfo cmd) {
        String triggerPrefix = cmd.getTriggerPrefix().toLowerCase();
        if (triggerPrefix.startsWith("!") || triggerPrefix.startsWith("/")) {
            return CommandType.GLOBAL;
        }
        return CommandType.GROUP; // 默认群聊指令
    }

    // ==================== 全局权限管理 ====================

    /**
     * 保存全局权限（遵循 Cache-Aside：先删缓存再更新 DB）
     */
    public void saveGlobalPermission(GlobalPermission permission) {
        permission.setUpdateTime(LocalDateTime.now());

        // 1. 先删除 Redis 缓存
        redisStore.deleteGlobalPermission(permission.getCommandName());

        // 2. 更新 MongoDB
        GlobalPermission saved = globalPermRepository.save(permission);

        // 3. 重新加载到 Redis
        redisStore.saveGlobalPermission(saved);

        log.info("Saved global permission: {}", saved.getCommandName());
    }

    /**
     * 删除全局权限
     */
    public void deleteGlobalPermission(String commandName) {
        redisStore.deleteGlobalPermission(commandName);
        globalPermRepository.findByCommandName(commandName)
                .ifPresent(globalPermRepository::delete);
        log.info("Deleted global permission: {}", commandName);
    }

    /**
     * 获取全局权限
     */
    public Optional<GlobalPermission> getGlobalPermission(String commandName) {
        Optional<GlobalPermission> cached = redisStore.getGlobalPermission(commandName);
        if (cached.isPresent()) {
            return cached;
        }

        Optional<GlobalPermission> fromDb = globalPermRepository.findByCommandName(commandName);
        if (fromDb.isPresent()) {
            redisStore.saveGlobalPermission(fromDb.get());
            return fromDb;
        }

        return Optional.empty();
    }

    // ==================== 群聊权限管理（需要时创建） ====================

    /**
     * 获取群聊权限（如果不存在则初始化默认权限）
     * 默认群聊权限：所有指令默认启用
     */
    public GroupPermission getOrInitGroupPermission(String groupId, String commandName) {
        // 1. 尝试从缓存获取
        Optional<GroupPermission> cached = redisStore.getGroupPermission(groupId, commandName);
        if (cached.isPresent()) {
            return cached.get();
        }

        // 2. 尝试从数据库获取
        Optional<GroupPermission> fromDb = groupPermRepository.findByGroupIdAndCommandName(groupId, commandName);
        if (fromDb.isPresent()) {
            redisStore.saveGroupPermission(fromDb.get());
            return fromDb.get();
        }

        // 3. 初始化该群聊的该指令默认权限（默认启用）
        GroupPermission defaultPerm = createDefaultGroupPermission(groupId, commandName);
        saveGroupPermission(defaultPerm);
        log.debug("Auto-created default group permission: group={}, cmd={}", groupId, commandName);
        return defaultPerm;
    }

    /**
     * 获取群聊所有权限（如果不存在则初始化）
     */
    public List<GroupPermission> getOrInitGroupAllPermissions(String groupId) {
        // 1. 尝试从缓存获取
        List<GroupPermission> cached = redisStore.getGroupAllPermissions(groupId);
        if (!cached.isEmpty()) {
            return cached;
        }

        // 2. 从数据库获取
        List<GroupPermission> fromDb = groupPermRepository.findByGroupId(groupId);
        if (!fromDb.isEmpty()) {
            // 缓存到Redis
            for (GroupPermission perm : fromDb) {
                redisStore.saveGroupPermission(perm);
            }
            return fromDb;
        }

        // 3. 初始化该群聊所有指令的默认权限
        return initGroupAllPermissions(groupId);
    }

    /**
     * 初始化群聊所有指令的默认权限（默认全部启用）
     */
    public List<GroupPermission> initGroupAllPermissions(String groupId) {
        Set<String> allCommands = redisStore.getAllGlobalCommands();
        List<GroupPermission> permissions = new ArrayList<>();

        for (String cmdName : allCommands) {
            // 检查是否已存在
            Optional<GroupPermission> existing = groupPermRepository.findByGroupIdAndCommandName(groupId, cmdName);
            if (existing.isPresent()) {
                permissions.add(existing.get());
                continue;
            }
            // 创建默认权限
            GroupPermission defaultPerm = createDefaultGroupPermission(groupId, cmdName);
            permissions.add(defaultPerm);
            saveGroupPermission(defaultPerm);
        }

        log.info("Initialized all permissions for group: {}, count={}", groupId, permissions.size());
        return permissions;
    }

    /**
     * 创建默认群聊权限
     */
    private GroupPermission createDefaultGroupPermission(String groupId, String commandName) {
        return GroupPermission.builder()
                .groupId(groupId)
                .commandName(commandName)
                .commandType(CommandType.GROUP)
                .enabled(true)  // 默认启用
                .allowedRoles(null)  // 使用指令默认角色
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }

    /**
     * 禁用群聊中的指定指令
     */
    public GroupPermission disableCommandInGroup(String groupId, String commandName) {
        GroupPermission perm = getOrInitGroupPermission(groupId, commandName);
        perm.setEnabled(false);
        saveGroupPermission(perm);
        log.info("Disabled command in group: group={}, cmd={}", groupId, commandName);
        return perm;
    }

    /**
     * 启用群聊中的指定指令
     */
    public GroupPermission enableCommandInGroup(String groupId, String commandName) {
        GroupPermission perm = getOrInitGroupPermission(groupId, commandName);
        perm.setEnabled(true);
        saveGroupPermission(perm);
        log.info("Enabled command in group: group={}, cmd={}", groupId, commandName);
        return perm;
    }

    /**
     * 保存群聊权限（遵循 Cache-Aside）
     */
    public void saveGroupPermission(GroupPermission permission) {
        permission.setUpdateTime(LocalDateTime.now());

        redisStore.deleteGroupPermission(permission.getGroupId(), permission.getCommandName());
        groupPermRepository.save(permission);
        redisStore.saveGroupPermission(permission);

        log.debug("Saved group permission: group={}, cmd={}", permission.getGroupId(), permission.getCommandName());
    }

    /**
     * 删除群聊特定指令权限
     */
    public void deleteGroupPermission(String groupId, String commandName) {
        redisStore.deleteGroupPermission(groupId, commandName);
        groupPermRepository.deleteByGroupIdAndCommandName(groupId, commandName);
        log.info("Deleted group permission: group={}, cmd={}", groupId, commandName);
    }

    /**
     * 删除群聊所有权限
     */
    public void deleteGroupAllPermissions(String groupId) {
        redisStore.deleteGroupAllPermissions(groupId);
        groupPermRepository.deleteByGroupId(groupId);
        log.info("Deleted all group permissions: group={}", groupId);
    }

    // ==================== 用户权限管理（自动初始化） ====================

    /**
     * 获取用户权限，如果不存在则创建默认权限并保存
     * 默认用户权限：允许使用所有指令
     */
    public UserPermission getOrInitUserPermission(String userId) {
        // 1. 尝试从缓存获取
        Optional<UserPermission> cached = redisStore.getUserPermission(userId);
        if (cached.isPresent()) {
            return cached.get();
        }

        // 2. 尝试从数据库获取
        Optional<UserPermission> fromDb = userPermRepository.findByUserId(userId);
        if (fromDb.isPresent()) {
            redisStore.saveUserPermission(fromDb.get());
            return fromDb.get();
        }

        // 3. 创建默认用户权限（允许所有指令）
        UserPermission defaultPerm = createDefaultUserPermission(userId);
        saveUserPermission(defaultPerm);
        log.debug("Auto-created default user permission: user={}", userId);
        return defaultPerm;
    }

    /**
     * 创建默认用户权限
     */
    private UserPermission createDefaultUserPermission(String userId) {
        return UserPermission.builder()
                .userId(userId)
                .allowedCommands(null)  // null 表示允许所有指令
                .deniedCommands(null)
                .botAdmin(false)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }

    /**
     * 保存用户权限（遵循 Cache-Aside）
     */
    public void saveUserPermission(UserPermission permission) {
        permission.setUpdateTime(LocalDateTime.now());

        redisStore.deleteUserPermission(permission.getUserId());
        UserPermission saved = userPermRepository.save(permission);
        redisStore.saveUserPermission(saved);

        log.info("Saved user permission: user={}", saved.getUserId());
    }

    /**
     * 检查用户是否是机器人管理员
     */
    public boolean isBotAdmin(String userId) {
        return redisStore.isBotAdmin(userId);
    }

    // ==================== 权限校验 ====================

    /**
     * 检查指令是否允许执行
     *
     * 权限校验流程：
     * 1. 超级管理员 → 全部允许
     * 2. 全局黑名单 → 拒绝
     * 3. 全局白名单 → 允许
     * 4. 群聊指令需检查群配置（如果存在）：
     *    - 群黑名单 → 拒绝
     *    - 群白名单 → 允许
     *    - 使用群权限的角色限制（如果未设置则使用指令的 roleType）
     * 5. 用户个人权限（自动初始化）→ 指令黑名单/白名单
     * 6. 权限级别检查
     */
    public PermissionCheckResult checkPermission(String commandName, String groupId, String userId, String senderRole) {
        PermissionCheckResult result = new PermissionCheckResult();

        // 0. 超级管理员拥有最高权限
        if (isBotAdmin(userId)) {
            return result.success();
        }

        // 1. 检查全局权限
        Optional<GlobalPermission> globalPerm = getGlobalPermission(commandName);
        if (globalPerm.isEmpty() || !globalPerm.get().isEnabled()) {
            return result.fail("指令未注册或已禁用");
        }

        GlobalPermission gp = globalPerm.get();

        // 2. 检查全局黑名单
        if (gp.isInGlobalBlacklist(userId)) {
            return result.fail("您已被全局禁止使用此指令");
        }

        // 3. 检查全局白名单（白名单优先级最高）
        if (gp.isInGlobalWhitelist(userId)) {
            return result.success();
        }

        // 4. 群聊指令需检查群配置
        if (gp.getCommandType() == CommandType.GROUP && groupId != null) {
            GroupPermission groupPerm = getOrInitGroupPermission(groupId, commandName);

            // 4.1 检查是否在群聊中被禁用
            if (!groupPerm.isEnabled()) {
                return result.fail("该指令在此群已被禁用");
            }

            // 4.2 检查群黑名单
            if (groupPerm.isInBlacklist(userId)) {
                return result.fail("您在该群已被禁止使用此指令");
            }

            // 4.3 检查群白名单
            if (groupPerm.isInWhitelist(userId)) {
                return result.success();
            }

            // 4.4 检查群权限的角色限制
            // 如果群权限设置了 allowedRoles，则使用群权限的角色限制
            // 否则使用指令的 roleType 进行检查
            if (groupPerm.getAllowedRoles() != null && !groupPerm.getAllowedRoles().isEmpty()) {
                if (!isRoleAllowed(senderRole, groupPerm.getAllowedRoles())) {
                    return result.fail("您的角色无权使用此指令");
                }
            } else {
                // 使用指令的 roleType 进行角色层级检查
                if (!isCommandRoleAllowed(senderRole, gp.getRoleType())) {
                    return result.fail("您的角色无权使用此指令");
                }
            }
        }

        // 5. 检查用户个人权限（自动初始化默认权限）
        UserPermission userPerm = getOrInitUserPermission(userId);
        if (!userPerm.isCommandAllowed(commandName)) {
            return result.fail("您已被禁止使用此指令");
        }

        // 6. 检查权限级别
        if (!checkPermissionLevel(gp.getLevel(), senderRole, groupId, userId)) {
            return result.fail("权限不足");
        }

        return result.success();
    }

    /**
     * 检查角色是否在允许列表中（用于群聊权限的简单列表匹配）
     * - allowedRoles 为 null 或空 → 返回 true（使用指令默认 roleType）
     * - 否则检查列表中是否包含 senderRole
     */
    private boolean isRoleAllowed(String senderRole, List<String> allowedRoles) {
        if (allowedRoles == null || allowedRoles.isEmpty()) {
            return true;
        }
        return allowedRoles.contains(senderRole) || allowedRoles.contains("default");
    }

    /**
     * 检查发送者角色是否满足指令要求的角色层级
     * <p>
     * 角色层级：
     * - System: 可以调用 System、Admin、User 指令
     * - Admin: 可以调用 Admin、User 指令
     * - User: 只能调用 User 指令
     * <p>
     * QQ 群角色映射：
     * - owner → System
     * - admin → Admin
     * - member → User
     *
     * @param senderRole QQ 群角色 (owner/admin/member)
     * @param commandRoleType 指令要求的角色类型 (System/Admin/User)
     * @return 是否允许执行
     */
    private boolean isCommandRoleAllowed(String senderRole, String commandRoleType) {
        // 如果指令没有设置 roleType，默认使用 User（最严格的限制）
        if (commandRoleType == null || commandRoleType.isEmpty()) {
            commandRoleType = RoleType.User.name();
        }

        // 将 senderRole 转换为 RoleType
        RoleType senderType = convertSenderRoleToRoleType(senderRole);
        RoleType requiredType = RoleType.valueOf(commandRoleType);

        // 根据层级检查权限
        return canAccessRole(senderType, requiredType);
    }

    /**
     * 将 QQ 群角色转换为 RoleType
     */
    private RoleType convertSenderRoleToRoleType(String senderRole) {
        if (senderRole == null) {
            return RoleType.User;  // 默认当作普通成员
        }
        return switch (senderRole.toLowerCase()) {
            case "owner" -> RoleType.System;
            case "admin" -> RoleType.Admin;
            default -> RoleType.User;
        };
    }

    /**
     * 检查发送者角色是否可以访问所需角色层级的指令
     * 层级顺序：System > Admin > User
     *
     * @param senderType 发送者的角色类型
     * @param requiredType 指令要求的角色类型
     * @return 是否可以访问
     */
    private boolean canAccessRole(RoleType senderType, RoleType requiredType) {
        // System 可以访问所有指令
        if (senderType == RoleType.System) {
            return true;
        }
        // Admin 可以访问 Admin 和 User 指令
        if (senderType == RoleType.Admin) {
            return requiredType != RoleType.System;
        }
        // User 只能访问 User 指令
        return requiredType == RoleType.User;
    }

    /**
     * 检查权限级别
     */
    private boolean checkPermissionLevel(PermissionLevel level, String senderRole, String groupId, String userId) {
        return switch (level) {
            case PUBLIC -> true;
            case USER -> userId != null;
            case GROUP_ADMIN -> "admin".equals(senderRole) || "owner".equals(senderRole);
            case GROUP_OWNER -> "owner".equals(senderRole);
            case BOT_ADMIN -> isBotAdmin(userId);
            case WHITELIST -> {
                Optional<GlobalPermission> perm = getGlobalPermission("");
                yield perm.map(p -> p.isInGlobalWhitelist(userId)).orElse(false);
            }
            case BLACKLIST -> {
                Optional<GlobalPermission> perm = getGlobalPermission("");
                yield !perm.map(p -> p.isInGlobalBlacklist(userId)).orElse(true);
            }
        };
    }

    /**
     * 获取用户可用的指令列表
     */
    public List<String> getAvailableCommands(String groupId, String userId, String senderRole) {
        Set<String> allCommands = redisStore.getAllGlobalCommands();
        List<String> available = new ArrayList<>();

        for (String cmd : allCommands) {
            if (checkPermission(cmd, groupId, userId, senderRole).isSuccess()) {
                available.add(cmd);
            }
        }

        return available;
    }
}
