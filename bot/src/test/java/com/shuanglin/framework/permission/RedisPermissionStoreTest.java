package com.shuanglin.framework.permission;

import com.shuanglin.common.enums.CommandType;
import com.shuanglin.dao.bot.GroupPermission;
import com.shuanglin.dao.bot.UserPermission;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Redis权限存储测试
 */
@ExtendWith(MockitoExtension.class)
class RedisPermissionStoreTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private SetOperations<String, Object> setOperations;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    private RedisPermissionStore redisPermissionStore;

    @BeforeEach
    void setUp() {
        redisPermissionStore = new RedisPermissionStore(redisTemplate);

        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(redisTemplate.opsForSet()).thenReturn(setOperations);
        lenient().when(redisTemplate.opsForHash()).thenReturn(hashOperations);
    }

    @Test
    @DisplayName("测试保存和获取全局权限")
    void testSaveAndGetGlobalPermission() {
        com.shuanglin.dao.bot.GlobalPermission perm = com.shuanglin.dao.bot.GlobalPermission.builder()
                .commandName("pig")
                .triggerPrefix("渚")
                .commandType(CommandType.GROUP)
                .enabled(true)
                .build();

        redisPermissionStore.saveGlobalPermission(perm);

        verify(valueOperations).set(
                eq("perm:global:pig"),
                anyString(),
                anyLong(),
                any()
        );
        verify(setOperations).add(eq("perm:commands:all"), eq("pig"));
    }

    @Test
    @DisplayName("测试删除全局权限")
    void testDeleteGlobalPermission() {
        redisPermissionStore.deleteGlobalPermission("pig");

        verify(redisTemplate).delete("perm:global:pig");
        verify(setOperations).remove(eq("perm:commands:all"), eq("pig"));
    }

    @Test
    @DisplayName("测试获取所有全局命令")
    void testGetAllGlobalCommands() {
        Set<Object> members = new HashSet<>(Arrays.asList("pig", "chat", "help"));
        when(setOperations.members("perm:commands:all")).thenReturn(members);

        Set<String> result = redisPermissionStore.getAllGlobalCommands();

        assertEquals(3, result.size());
        assertTrue(result.contains("pig"));
    }

    @Test
    @DisplayName("测试获取命令类型")
    void testGetCommandType() {
        when(valueOperations.get("perm:cmd:type:pig")).thenReturn("GROUP");

        Optional<CommandType> result = redisPermissionStore.getCommandType("pig");

        assertTrue(result.isPresent());
        assertEquals(CommandType.GROUP, result.get());
    }

    @Test
    @DisplayName("测试保存和获取群聊权限")
    void testSaveAndGetGroupPermission() {
        GroupPermission perm = GroupPermission.builder()
                .groupId("345693826")
                .commandName("pig")
                .commandType(CommandType.GROUP)
                .enabled(true)
                .whitelist(List.of("1751649231"))
                .build();

        redisPermissionStore.saveGroupPermission(perm);

        verify(valueOperations).set(
                eq("perm:group:345693826:pig"),
                anyString(),
                anyLong(),
                any()
        );
    }

    @Test
    @DisplayName("测试清空所有缓存")
    void testClearAllCache() {
        when(redisTemplate.keys(anyString())).thenReturn(Set.of("key1", "key2"));

        redisPermissionStore.clearAllCache();

        // 验证调用了 delete(keys) 和 delete("perm:commands:all")
        verify(redisTemplate, atLeast(1)).delete(anySet());
        verify(redisTemplate).delete("perm:commands:all");
    }

    @Test
    @DisplayName("测试保存和获取用户权限")
    void testSaveAndGetUserPermission() {
        UserPermission perm = UserPermission.builder()
                .userId("1751649231")
                .allowedCommands(List.of("pig", "chat"))
                .deniedCommands(List.of())
                .botAdmin(false)
                .build();

        redisPermissionStore.saveUserPermission(perm);

        verify(valueOperations).set(
                eq("perm:user:1751649231"),
                anyString(),
                anyLong(),
                any()
        );
    }
}
