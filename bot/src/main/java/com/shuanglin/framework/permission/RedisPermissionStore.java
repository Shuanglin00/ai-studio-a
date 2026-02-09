package com.shuanglin.framework.permission;

import com.google.gson.Gson;
import com.shuanglin.common.enums.CommandType;
import com.shuanglin.dao.bot.GlobalPermission;
import com.shuanglin.dao.bot.GroupPermission;
import com.shuanglin.dao.bot.UserPermission;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis 权限存储
 * Cache-Aside 模式：读取优先 Redis，变更时先删 Redis 再更新 DB
 */
@Slf4j
@Component
public class RedisPermissionStore {

    private final RedisTemplate<String, Object> redisTemplate;
    private final Gson gson = new Gson();

    // Redis Key 前缀
    private static final String GLOBAL_PERM_PREFIX = "perm:global:";
    private static final String GROUP_PERM_PREFIX = "perm:group:";
    private static final String GROUP_ALL_PERMS_PREFIX = "perm:group:all:";
    private static final String USER_PERM_PREFIX = "perm:user:";
    private static final String COMMAND_TYPE_PREFIX = "perm:cmd:type:";
    private static final String ALL_COMMANDS_SET = "perm:commands:all";

    private static final long TTL = 24; // 小时

    // 超级管理员列表（最高权限，可调用所有指令）
    private static final Set<String> SUPER_ADMINS = Set.of(
            "1751649231"
    );

    public RedisPermissionStore(@Qualifier("permissionRedisTemplate") RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // ==================== 全局权限操作 ====================

    /**
     * 获取全局权限配置
     */
    public Optional<GlobalPermission> getGlobalPermission(String commandName) {
        String key = GLOBAL_PERM_PREFIX + commandName;
        Object value = redisTemplate.opsForValue().get(key);
        if (value != null) {
            return Optional.of(gson.fromJson(value.toString(), GlobalPermission.class));
        }
        return Optional.empty();
    }

    /**
     * 保存全局权限（仅更新缓存）
     */
    public void saveGlobalPermission(GlobalPermission permission) {
        String key = GLOBAL_PERM_PREFIX + permission.getCommandName();
        redisTemplate.opsForValue().set(key, gson.toJson(permission), TTL, TimeUnit.HOURS);

        // 更新命令列表
        redisTemplate.opsForSet().add(ALL_COMMANDS_SET, permission.getCommandName());

        // 更新命令类型缓存
        String typeKey = COMMAND_TYPE_PREFIX + permission.getCommandName();
        redisTemplate.opsForValue().set(typeKey, permission.getCommandType().name(), TTL, TimeUnit.HOURS);

        log.debug("Cached global permission: {}", permission.getCommandName());
    }

    /**
     * 删除全局权限缓存
     */
    public void deleteGlobalPermission(String commandName) {
        String key = GLOBAL_PERM_PREFIX + commandName;
        redisTemplate.delete(key);
        redisTemplate.opsForSet().remove(ALL_COMMANDS_SET, commandName);
        redisTemplate.delete(COMMAND_TYPE_PREFIX + commandName);
        log.debug("Deleted global permission cache: {}", commandName);
    }

    /**
     * 获取所有全局权限命令名
     */
    public Set<String> getAllGlobalCommands() {
        Set<Object> members = redisTemplate.opsForSet().members(ALL_COMMANDS_SET);
        if (members == null || members.isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> result = new HashSet<>();
        for (Object member : members) {
            result.add(member.toString());
        }
        return result;
    }

    /**
     * 获取命令类型
     */
    public Optional<CommandType> getCommandType(String commandName) {
        String key = COMMAND_TYPE_PREFIX + commandName;
        Object value = redisTemplate.opsForValue().get(key);
        if (value != null) {
            return Optional.of(CommandType.valueOf(value.toString()));
        }
        return Optional.empty();
    }

    // ==================== 群权限操作 ====================

    /**
     * 获取群聊特定指令的权限
     */
    public Optional<GroupPermission> getGroupPermission(String groupId, String commandName) {
        String key = GROUP_PERM_PREFIX + groupId + ":" + commandName;
        Object value = redisTemplate.opsForValue().get(key);
        if (value != null) {
            return Optional.of(gson.fromJson(value.toString(), GroupPermission.class));
        }
        return Optional.empty();
    }

    /**
     * 获取群聊所有权限配置
     */
    public List<GroupPermission> getGroupAllPermissions(String groupId) {
        String key = GROUP_ALL_PERMS_PREFIX + groupId;
        List<Object> values = redisTemplate.opsForHash().values(key);
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }
        List<GroupPermission> result = new ArrayList<>();
        for (Object value : values) {
            result.add(gson.fromJson(value.toString(), GroupPermission.class));
        }
        return result;
    }

    /**
     * 保存群聊权限（仅更新缓存）
     */
    public void saveGroupPermission(GroupPermission permission) {
        String key = GROUP_PERM_PREFIX + permission.getGroupId() + ":" + permission.getCommandName();
        redisTemplate.opsForValue().set(key, gson.toJson(permission), TTL, TimeUnit.HOURS);

        // 更新群聊所有权限缓存
        String allKey = GROUP_ALL_PERMS_PREFIX + permission.getGroupId();
        redisTemplate.opsForHash().put(allKey, permission.getCommandName(), gson.toJson(permission));

        log.debug("Cached group permission: group={}, cmd={}", permission.getGroupId(), permission.getCommandName());
    }

    /**
     * 删除群聊特定指令权限缓存
     */
    public void deleteGroupPermission(String groupId, String commandName) {
        String key = GROUP_PERM_PREFIX + groupId + ":" + commandName;
        redisTemplate.delete(key);

        String allKey = GROUP_ALL_PERMS_PREFIX + groupId;
        redisTemplate.opsForHash().delete(allKey, commandName);

        log.debug("Deleted group permission cache: group={}, cmd={}", groupId, commandName);
    }

    /**
     * 删除群聊所有权限缓存
     */
    public void deleteGroupAllPermissions(String groupId) {
        // 删除单个权限缓存
        List<GroupPermission> perms = getGroupAllPermissions(groupId);
        for (GroupPermission perm : perms) {
            redisTemplate.delete(GROUP_PERM_PREFIX + groupId + ":" + perm.getCommandName());
        }

        // 删除群聊所有权限缓存
        String allKey = GROUP_ALL_PERMS_PREFIX + groupId;
        redisTemplate.delete(allKey);

        log.debug("Deleted all group permission cache: group={}", groupId);
    }

    // ==================== 用户权限操作 ====================

    /**
     * 获取用户权限配置
     */
    public Optional<UserPermission> getUserPermission(String userId) {
        String key = USER_PERM_PREFIX + userId;
        Object value = redisTemplate.opsForValue().get(key);
        if (value != null) {
            return Optional.of(gson.fromJson(value.toString(), UserPermission.class));
        }
        return Optional.empty();
    }

    /**
     * 保存用户权限（仅更新缓存）
     */
    public void saveUserPermission(UserPermission permission) {
        String key = USER_PERM_PREFIX + permission.getUserId();
        redisTemplate.opsForValue().set(key, gson.toJson(permission), TTL, TimeUnit.HOURS);
        log.debug("Cached user permission: user={}", permission.getUserId());
    }

    /**
     * 删除用户权限缓存
     */
    public void deleteUserPermission(String userId) {
        String key = USER_PERM_PREFIX + userId;
        redisTemplate.delete(key);
        log.debug("Deleted user permission cache: user={}", userId);
    }

    /**
     * 检查用户是否是机器人管理员（快速检查）
     * 超级管理员拥有最高权限
     */
    public boolean isBotAdmin(String userId) {
        // 先检查是否是超级管理员
        if (SUPER_ADMINS.contains(userId)) {
            return true;
        }
        // 再检查数据库中的管理员配置
        Optional<UserPermission> perm = getUserPermission(userId);
        return perm.map(UserPermission::isBotAdmin).orElse(false);
    }

    // ==================== 批量操作 ====================

    /**
     * 清空所有权限缓存
     */
    public void clearAllCache() {
        Set<String> keys = redisTemplate.keys(GLOBAL_PERM_PREFIX + "*");
        if (keys != null) {
            redisTemplate.delete(keys);
        }
        keys = redisTemplate.keys(GROUP_PERM_PREFIX + "*");
        if (keys != null) {
            redisTemplate.delete(keys);
        }
        keys = redisTemplate.keys(GROUP_ALL_PERMS_PREFIX + "*");
        if (keys != null) {
            redisTemplate.delete(keys);
        }
        keys = redisTemplate.keys(USER_PERM_PREFIX + "*");
        if (keys != null) {
            redisTemplate.delete(keys);
        }
        redisTemplate.delete(ALL_COMMANDS_SET);
        log.info("Cleared all permission cache");
    }
}
