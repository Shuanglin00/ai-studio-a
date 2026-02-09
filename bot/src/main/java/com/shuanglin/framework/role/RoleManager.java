package com.shuanglin.framework.role;

import com.shuanglin.dao.bot.BotRole;
import com.shuanglin.dao.bot.BotRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色管理器
 * 管理机器人角色的CRUD操作及角色与指令的映射关系
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RoleManager {

    private final BotRoleRepository botRoleRepository;
    
    @Qualifier("botRoleRedisTemplate")
    private final RedisTemplate<String, BotRole> botRoleRedisTemplate;

    private static final String ROLE_CACHE_KEY = "bot:roles";

    /**
     * 启动时从MongoDB加载所有角色到Redis
     */
    @EventListener(ContextRefreshedEvent.class)
    public void initializeRoles() {
        log.info("Role Manager: Initializing roles from MongoDB...");
        try {
            List<BotRole> roles = botRoleRepository.findAll();
            if (roles.isEmpty()) {
                log.warn("No roles found in database, creating default roles...");
                createDefaultRoles();
                roles = botRoleRepository.findAll();
            }
            
            // 加载到Redis
            HashOperations<String, String, BotRole> hashOps = botRoleRedisTemplate.opsForHash();
            for (BotRole role : roles) {
                hashOps.put(ROLE_CACHE_KEY, role.getRoleName(), role);
            }
            
            log.info("Role Manager: Loaded {} roles to Redis cache.", roles.size());
        } catch (Exception e) {
            log.error("Failed to initialize roles", e);
        }
    }

    /**
     * 创建默认角色
     */
    private void createDefaultRoles() {
        // 创建默认"AI助手"角色
        BotRole aiRole = BotRole.builder()
                .roleName("AI助手")
                .description("提供智能对话功能")
                .commandNames(List.of("chat", "publish_model", "switch_model"))
                .isActive(true)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        botRoleRepository.save(aiRole);
        
        // 创建默认"娱乐机器人"角色
        BotRole funRole = BotRole.builder()
                .roleName("娱乐机器人")
                .description("提供娱乐功能")
                .commandNames(List.of("pig"))
                .isActive(true)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        botRoleRepository.save(funRole);
        
        log.info("Created default roles: AI助手, 娱乐机器人");
    }

    /**
     * 创建新角色
     */
    public BotRole createRole(BotRole role) {
        role.setCreateTime(LocalDateTime.now());
        role.setUpdateTime(LocalDateTime.now());
        
        // 保存到MongoDB
        BotRole saved = botRoleRepository.save(role);
        
        // 同步到Redis
        HashOperations<String, String, BotRole> hashOps = botRoleRedisTemplate.opsForHash();
        hashOps.put(ROLE_CACHE_KEY, role.getRoleName(), saved);
        
        log.info("Created role: {}", role.getRoleName());
        return saved;
    }

    /**
     * 更新角色信息
     */
    public BotRole updateRole(BotRole role) {
        role.setUpdateTime(LocalDateTime.now());
        
        // 更新MongoDB
        BotRole updated = botRoleRepository.save(role);
        
        // 删除Redis缓存，下次查询时重建
        HashOperations<String, String, BotRole> hashOps = botRoleRedisTemplate.opsForHash();
        hashOps.delete(ROLE_CACHE_KEY, role.getRoleName());
        
        log.info("Updated role: {}", role.getRoleName());
        return updated;
    }

    /**
     * 根据角色名称查询
     */
    public BotRole getRoleByName(String roleName) {
        // 先查Redis
        HashOperations<String, String, BotRole> hashOps = botRoleRedisTemplate.opsForHash();
        BotRole role = hashOps.get(ROLE_CACHE_KEY, roleName);
        
        if (role != null) {
            return role;
        }
        
        // Redis未命中，查询MongoDB
        role = botRoleRepository.findByRoleName(roleName).orElse(null);
        if (role != null) {
            // 回填Redis
            hashOps.put(ROLE_CACHE_KEY, roleName, role);
        }
        
        return role;
    }

    /**
     * 获取所有启用角色
     */
    public List<BotRole> getAllActiveRoles() {
        // 从Redis获取所有角色
        HashOperations<String, String, BotRole> hashOps = botRoleRedisTemplate.opsForHash();
        List<BotRole> roles = hashOps.values(ROLE_CACHE_KEY);
        
        if (roles == null || roles.isEmpty()) {
            // Redis为空，从MongoDB加载
            roles = botRoleRepository.findByIsActive(true);
            for (BotRole role : roles) {
                hashOps.put(ROLE_CACHE_KEY, role.getRoleName(), role);
            }
        } else {
            // 过滤出启用的角色
            roles = roles.stream()
                    .filter(role -> role.getIsActive() != null && role.getIsActive())
                    .collect(Collectors.toList());
        }
        
        return roles;
    }

    /**
     * 绑定指令到角色
     */
    public void bindCommandsToRole(String roleName, List<String> commands) {
        BotRole role = getRoleByName(roleName);
        if (role == null) {
            throw new IllegalArgumentException("Role not found: " + roleName);
        }
        
        // 合并指令列表（去重）
        List<String> commandNames = role.getCommandNames();
        if (commandNames == null) {
            commandNames = new ArrayList<>();
        }
        for (String cmd : commands) {
            if (!commandNames.contains(cmd)) {
                commandNames.add(cmd);
            }
        }
        role.setCommandNames(commandNames);
        
        updateRole(role);
        log.info("Bound commands {} to role: {}", commands, roleName);
    }

    /**
     * 获取所有角色
     */
    public List<BotRole> getAllRoles() {
        HashOperations<String, String, BotRole> hashOps = botRoleRedisTemplate.opsForHash();
        List<BotRole> roles = hashOps.values(ROLE_CACHE_KEY);
        
        if (roles == null || roles.isEmpty()) {
            roles = botRoleRepository.findAll();
            for (BotRole role : roles) {
                hashOps.put(ROLE_CACHE_KEY, role.getRoleName(), role);
            }
        }
        
        return roles;
    }
}
