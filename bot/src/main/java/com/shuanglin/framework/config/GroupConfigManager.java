package com.shuanglin.framework.config;

import com.shuanglin.dao.bot.GroupConfiguration;
import com.shuanglin.dao.bot.GroupConfigurationRepository;
import com.shuanglin.dao.bot.BotRole;
import com.shuanglin.dao.model.Model;
import com.shuanglin.dao.model.ModelsRepository;
import com.shuanglin.framework.role.RoleManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 群聊配置管理器
 * 管理群聊级别的配置，替代原GroupInfoUtil
 */
@Slf4j
@Component
public class GroupConfigManager {

    private final GroupConfigurationRepository groupConfigRepository;
    private final RoleManager roleManager;
    private final ModelsRepository modelsRepository;

    @Qualifier("groupConfigRedisTemplate")
    private final RedisTemplate<String, GroupConfiguration> groupConfigRedisTemplate;

    @Qualifier("groupInfoRedisTemplate")
    private final RedisTemplate<String, Object> groupInfoRedisTemplate;

    private static final String CONFIG_CACHE_PREFIX = "group:config:";
    private static final String ADMINS_CACHE_PREFIX = "group:admins:";
    private static final long CONFIG_TTL = 24; // 24小时

    @Autowired
    public GroupConfigManager(
            GroupConfigurationRepository groupConfigRepository,
            RoleManager roleManager,
            ModelsRepository modelsRepository,
            @Qualifier("groupConfigRedisTemplate") RedisTemplate<String, GroupConfiguration> groupConfigRedisTemplate,
            @Qualifier("groupInfoRedisTemplate") RedisTemplate<String, Object> groupInfoRedisTemplate) {
        this.groupConfigRepository = groupConfigRepository;
        this.roleManager = roleManager;
        this.modelsRepository = modelsRepository;
        this.groupConfigRedisTemplate = groupConfigRedisTemplate;
        this.groupInfoRedisTemplate = groupInfoRedisTemplate;
    }

    /**
     * 获取群聊配置
     */
    public GroupConfiguration getGroupConfig(String groupId) {
        String cacheKey = CONFIG_CACHE_PREFIX + groupId;

        // 先查Redis
        GroupConfiguration config = groupConfigRedisTemplate.opsForValue().get(cacheKey);
        if (config != null) {
            return config;
        }

        // Redis未命中，查询MongoDB
        config = groupConfigRepository.findByGroupId(groupId).orElse(null);
        if (config != null) {
            // 回填Redis
            groupConfigRedisTemplate.opsForValue().set(cacheKey, config, CONFIG_TTL, TimeUnit.HOURS);
            return config;
        }

        // 数据库也无配置，创建默认配置
        return createDefaultConfig(groupId);
    }

    /**
     * 创建默认配置
     */
    public GroupConfiguration createDefaultConfig(String groupId) {
        log.info("Creating default configuration for group: {}", groupId);

        // 获取所有启用的角色
        List<BotRole> activeRoles = roleManager.getAllActiveRoles();
        List<String> enabledRoles = activeRoles.stream()
                .map(BotRole::getRoleName)
                .collect(Collectors.toList());

        // 获取默认AI模型
        List<Model> activeModels = modelsRepository.getModelsByIsActive("true");
        Model defaultModel = activeModels.isEmpty() ? null : activeModels.get(0);

        GroupConfiguration config = GroupConfiguration.builder()
                .groupId(groupId)
                .enabledRoles(enabledRoles)
                .admins(new ArrayList<>())
                .modelConfig(defaultModel)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        // 保存到MongoDB
        config = groupConfigRepository.save(config);

        // 写入Redis缓存
        String cacheKey = CONFIG_CACHE_PREFIX + groupId;
        groupConfigRedisTemplate.opsForValue().set(cacheKey, config, CONFIG_TTL, TimeUnit.HOURS);

        log.info("Created default configuration for group: {}, enabled roles: {}", groupId, enabledRoles);
        return config;
    }

    /**
     * 为群聊启用角色
     */
    public void enableRole(String groupId, String roleName) {
        GroupConfiguration config = getGroupConfig(groupId);
        
        List<String> enabledRoles = config.getEnabledRoles();
        if (enabledRoles == null) {
            enabledRoles = new ArrayList<>();
            config.setEnabledRoles(enabledRoles);
        }
        
        if (!enabledRoles.contains(roleName)) {
            enabledRoles.add(roleName);
            config.setUpdateTime(LocalDateTime.now());
            
            // 更新MongoDB
            groupConfigRepository.save(config);
            
            // 删除缓存
            groupConfigRedisTemplate.delete(CONFIG_CACHE_PREFIX + groupId);
            
            log.info("Enabled role {} for group {}", roleName, groupId);
        }
    }

    /**
     * 禁用群聊角色
     */
    public void disableRole(String groupId, String roleName) {
        GroupConfiguration config = getGroupConfig(groupId);
        
        List<String> enabledRoles = config.getEnabledRoles();
        if (enabledRoles != null && enabledRoles.remove(roleName)) {
            config.setUpdateTime(LocalDateTime.now());
            
            // 更新MongoDB
            groupConfigRepository.save(config);
            
            // 删除缓存
            groupConfigRedisTemplate.delete(CONFIG_CACHE_PREFIX + groupId);
            
            log.info("Disabled role {} for group {}", roleName, groupId);
        }
    }

    /**
     * 添加管理员
     */
    public void addAdmin(String groupId, String userId) {
        GroupConfiguration config = getGroupConfig(groupId);
        
        List<String> admins = config.getAdmins();
        if (admins == null) {
            admins = new ArrayList<>();
            config.setAdmins(admins);
        }
        
        if (!admins.contains(userId)) {
            admins.add(userId);
            config.setUpdateTime(LocalDateTime.now());
            
            // 更新MongoDB
            groupConfigRepository.save(config);
            
            // 删除缓存
            groupConfigRedisTemplate.delete(CONFIG_CACHE_PREFIX + groupId);
            groupInfoRedisTemplate.delete(ADMINS_CACHE_PREFIX + groupId);
            
            log.info("Added admin {} to group {}", userId, groupId);
        }
    }

    /**
     * 移除管理员
     */
    public void removeAdmin(String groupId, String userId) {
        GroupConfiguration config = getGroupConfig(groupId);
        
        List<String> admins = config.getAdmins();
        if (admins != null && admins.remove(userId)) {
            config.setUpdateTime(LocalDateTime.now());
            
            // 更新MongoDB
            groupConfigRepository.save(config);
            
            // 删除缓存
            groupConfigRedisTemplate.delete(CONFIG_CACHE_PREFIX + groupId);
            groupInfoRedisTemplate.delete(ADMINS_CACHE_PREFIX + groupId);
            
            log.info("Removed admin {} from group {}", userId, groupId);
        }
    }

    /**
     * 判断是否为管理员
     */
    public Boolean isAdmin(String groupId, String userId) {
        String cacheKey = ADMINS_CACHE_PREFIX + groupId;
        
        // 先查Redis Set缓存
        SetOperations<String, Object> setOps = groupInfoRedisTemplate.opsForSet();
        if (Boolean.TRUE.equals(groupInfoRedisTemplate.hasKey(cacheKey))) {
            return setOps.isMember(cacheKey, userId);
        }
        
        // 缓存未命中，从配置获取
        GroupConfiguration config = getGroupConfig(groupId);
        List<String> admins = config.getAdmins();
        
        if (admins != null && !admins.isEmpty()) {
            // 构建Set缓存
            for (String admin : admins) {
                setOps.add(cacheKey, admin);
            }
            groupInfoRedisTemplate.expire(cacheKey, CONFIG_TTL, TimeUnit.HOURS);
        }
        
        return admins != null && admins.contains(userId);
    }

    /**
     * 更新模型配置
     */
    public void updateModelConfig(String groupId, Model modelInfo) {
        GroupConfiguration config = getGroupConfig(groupId);
        config.setModelConfig(modelInfo);
        config.setUpdateTime(LocalDateTime.now());
        
        // 更新MongoDB
        groupConfigRepository.save(config);
        
        // 删除缓存
        groupConfigRedisTemplate.delete(CONFIG_CACHE_PREFIX + groupId);
        
        log.info("Updated model config for group {}", groupId);
    }
}
