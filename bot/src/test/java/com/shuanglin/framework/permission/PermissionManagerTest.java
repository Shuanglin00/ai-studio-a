package com.shuanglin.framework.permission;

import com.shuanglin.common.enums.CommandType;
import com.shuanglin.common.enums.PermissionLevel;
import com.shuanglin.dao.bot.*;
import com.shuanglin.framework.registry.CommandRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 权限管理器测试
 */
@ExtendWith(MockitoExtension.class)
class PermissionManagerTest {

    @Mock
    private GlobalPermissionRepository globalPermRepository;

    @Mock
    private GroupPermissionRepository groupPermRepository;

    @Mock
    private UserPermissionRepository userPermRepository;

    @Mock
    private RedisPermissionStore redisStore;

    @Mock
    private CommandRegistry commandRegistry;

    @InjectMocks
    private PermissionManager permissionManager;

    private GlobalPermission testGlobalPerm;
    private GroupPermission testGroupPerm;
    private UserPermission testUserPerm;

    @BeforeEach
    void setUp() {
        // 初始化全局权限（User 级别的指令）
        testGlobalPerm = GlobalPermission.builder()
                .commandName("pig")
                .triggerPrefix("渚")
                .commandType(CommandType.GROUP)
                .roleType("User")  // User 级别指令，普通成员可调用
                .level(PermissionLevel.PUBLIC)
                .enabled(true)
                .allowedRoles(null)  // 使用 roleType 进行角色检查
                .globalBlacklist(List.of())
                .globalWhitelist(List.of())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        // 初始化群聊权限
        testGroupPerm = GroupPermission.builder()
                .groupId("345693826")
                .commandName("pig")
                .commandType(CommandType.GROUP)
                .enabled(true)
                .blacklist(List.of())
                .whitelist(List.of("1751649231"))
                .allowedRoles(null)  // null 表示使用指令默认的 roleType
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        // 初始化用户权限
        testUserPerm = UserPermission.builder()
                .userId("1751649231")
                .allowedCommands(null)  // null 表示允许所有指令
                .deniedCommands(List.of())
                .botAdmin(false)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("测试获取全局权限 - Redis命中")
    void testGetGlobalPermission_RedisHit() {
        when(redisStore.getGlobalPermission("pig")).thenReturn(Optional.of(testGlobalPerm));

        Optional<GlobalPermission> result = permissionManager.getGlobalPermission("pig");

        assertTrue(result.isPresent());
        assertEquals("pig", result.get().getCommandName());
        verify(globalPermRepository, never()).findByCommandName(anyString());
    }

    @Test
    @DisplayName("测试获取全局权限 - Redis未命中，从MongoDB加载")
    void testGetGlobalPermission_RedisMiss() {
        when(redisStore.getGlobalPermission("pig")).thenReturn(Optional.empty());
        when(globalPermRepository.findByCommandName("pig")).thenReturn(Optional.of(testGlobalPerm));

        Optional<GlobalPermission> result = permissionManager.getGlobalPermission("pig");

        assertTrue(result.isPresent());
        assertEquals("pig", result.get().getCommandName());
        verify(redisStore).saveGlobalPermission(testGlobalPerm);
    }

    @Test
    @DisplayName("测试保存全局权限 - Cache-Aside模式")
    void testSaveGlobalPermission() {
        when(globalPermRepository.save(any(GlobalPermission.class))).thenReturn(testGlobalPerm);

        permissionManager.saveGlobalPermission(testGlobalPerm);

        // 验证：先删缓存，再更新DB，再加载到缓存
        verify(redisStore).deleteGlobalPermission("pig");
        verify(globalPermRepository).save(testGlobalPerm);
        verify(redisStore).saveGlobalPermission(testGlobalPerm);
    }

    @Test
    @DisplayName("测试删除全局权限 - Cache-Aside模式")
    void testDeleteGlobalPermission() {
        when(globalPermRepository.findByCommandName("pig")).thenReturn(Optional.of(testGlobalPerm));

        permissionManager.deleteGlobalPermission("pig");

        verify(redisStore).deleteGlobalPermission("pig");
        verify(globalPermRepository).delete(testGlobalPerm);
    }

    @Test
    @DisplayName("测试权限校验 - 全局黑名单")
    void testCheckPermission_GlobalBlacklist() {
        testGlobalPerm.setGlobalBlacklist(List.of("1751649231"));

        when(redisStore.getGlobalPermission("pig")).thenReturn(Optional.of(testGlobalPerm));

        PermissionCheckResult result = permissionManager.checkPermission(
                "pig", "345693826", "1751649231", "member"
        );

        assertFalse(result.isSuccess());
        assertEquals("您已被全局禁止使用此指令", result.getReason());
    }

    @Test
    @DisplayName("测试权限校验 - 群聊白名单优先")
    void testCheckPermission_GroupWhitelist() {
        when(redisStore.getGlobalPermission("pig")).thenReturn(Optional.of(testGlobalPerm));
        when(redisStore.getGroupPermission(eq("345693826"), eq("pig")))
                .thenReturn(Optional.of(testGroupPerm));

        PermissionCheckResult result = permissionManager.checkPermission(
                "pig", "345693826", "1751649231", "member"
        );

        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("测试权限校验 - 群聊黑名单")
    void testCheckPermission_GroupBlacklist() {
        testGroupPerm.setBlacklist(List.of("1751649231"));
        testGroupPerm.setWhitelist(List.of());

        when(redisStore.getGlobalPermission("pig")).thenReturn(Optional.of(testGlobalPerm));
        when(redisStore.getGroupPermission(eq("345693826"), eq("pig")))
                .thenReturn(Optional.of(testGroupPerm));

        PermissionCheckResult result = permissionManager.checkPermission(
                "pig", "345693826", "1751649231", "member"
        );

        assertFalse(result.isSuccess());
        assertEquals("您在该群已被禁止使用此指令", result.getReason());
    }

    @Test
    @DisplayName("测试权限校验 - 角色权限检查（群聊权限覆盖）")
    void testCheckPermission_RoleNotAllowed() {
        // 群聊权限设置了 allowedRoles，绕过指令的 roleType 检查
        testGroupPerm.setAllowedRoles(List.of("admin"));  // 只允许 admin
        testGroupPerm.setWhitelist(List.of());

        when(redisStore.getGlobalPermission("pig")).thenReturn(Optional.of(testGlobalPerm));
        when(redisStore.getGroupPermission(eq("345693826"), eq("pig")))
                .thenReturn(Optional.of(testGroupPerm));

        PermissionCheckResult result = permissionManager.checkPermission(
                "pig", "345693826", "1751649231", "member"
        );

        assertFalse(result.isSuccess());
        assertEquals("您的角色无权使用此指令", result.getReason());
    }

    @Test
    @DisplayName("测试权限校验 - User指令管理员可调用")
    void testCheckPermission_AdminCanCallUserCommand() {
        // User 级别的指令，admin 应该可以调用
        testGlobalPerm.setRoleType("User");

        when(redisStore.getGlobalPermission("pig")).thenReturn(Optional.of(testGlobalPerm));
        // 群聊权限不存在，使用指令的 roleType
        when(redisStore.getGroupPermission("345693826", "pig")).thenReturn(Optional.empty());
        when(groupPermRepository.findByGroupIdAndCommandName("345693826", "pig")).thenReturn(Optional.empty());
        // 用户权限自动初始化
        when(redisStore.getUserPermission("1751649231")).thenReturn(Optional.empty());
        when(userPermRepository.findByUserId("1751649231")).thenReturn(Optional.empty());
        lenient().when(userPermRepository.save(any(UserPermission.class))).thenAnswer(inv -> inv.getArgument(0));

        PermissionCheckResult result = permissionManager.checkPermission(
                "pig", "345693826", "1751649231", "admin"
        );

        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("测试权限校验 - Admin指令普通成员不可调用")
    void testCheckPermission_AdminCommand_MemberDenied() {
        // Admin 级别的指令，普通成员不应该可以调用
        testGlobalPerm.setRoleType("Admin");

        when(redisStore.getGlobalPermission("pig")).thenReturn(Optional.of(testGlobalPerm));
        // 群聊权限不存在，使用指令的 roleType
        when(redisStore.getGroupPermission("345693826", "pig")).thenReturn(Optional.empty());
        when(groupPermRepository.findByGroupIdAndCommandName("345693826", "pig")).thenReturn(Optional.empty());

        PermissionCheckResult result = permissionManager.checkPermission(
                "pig", "345693826", "1751649231", "member"
        );

        assertFalse(result.isSuccess());
        assertEquals("您的角色无权使用此指令", result.getReason());
    }

    @Test
    @DisplayName("测试权限校验 - Admin指令管理员可调用")
    void testCheckPermission_AdminCommand_AdminAllowed() {
        // Admin 级别的指令，admin 应该可以调用
        testGlobalPerm.setRoleType("Admin");

        when(redisStore.getGlobalPermission("pig")).thenReturn(Optional.of(testGlobalPerm));
        // 群聊权限不存在，使用指令的 roleType
        when(redisStore.getGroupPermission("345693826", "pig")).thenReturn(Optional.empty());
        when(groupPermRepository.findByGroupIdAndCommandName("345693826", "pig")).thenReturn(Optional.empty());
        // 用户权限自动初始化
        when(redisStore.getUserPermission("1751649231")).thenReturn(Optional.empty());
        when(userPermRepository.findByUserId("1751649231")).thenReturn(Optional.empty());
        lenient().when(userPermRepository.save(any(UserPermission.class))).thenAnswer(inv -> inv.getArgument(0));

        PermissionCheckResult result = permissionManager.checkPermission(
                "pig", "345693826", "1751649231", "admin"
        );

        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("测试权限校验 - System指令普通成员不可调用")
    void testCheckPermission_SystemCommand_MemberDenied() {
        // System 级别的指令，普通成员不应该可以调用
        testGlobalPerm.setRoleType("System");

        when(redisStore.getGlobalPermission("pig")).thenReturn(Optional.of(testGlobalPerm));
        // 群聊权限不存在，使用指令的 roleType
        when(redisStore.getGroupPermission("345693826", "pig")).thenReturn(Optional.empty());
        when(groupPermRepository.findByGroupIdAndCommandName("345693826", "pig")).thenReturn(Optional.empty());

        PermissionCheckResult result = permissionManager.checkPermission(
                "pig", "345693826", "1751649231", "member"
        );

        assertFalse(result.isSuccess());
        assertEquals("您的角色无权使用此指令", result.getReason());
    }

    @Test
    @DisplayName("测试权限校验 - System指令管理员不可调用")
    void testCheckPermission_SystemCommand_AdminDenied() {
        // System 级别的指令，admin 也不应该可以调用
        testGlobalPerm.setRoleType("System");

        when(redisStore.getGlobalPermission("pig")).thenReturn(Optional.of(testGlobalPerm));
        // 群聊权限不存在，使用指令的 roleType
        when(redisStore.getGroupPermission("345693826", "pig")).thenReturn(Optional.empty());
        when(groupPermRepository.findByGroupIdAndCommandName("345693826", "pig")).thenReturn(Optional.empty());

        PermissionCheckResult result = permissionManager.checkPermission(
                "pig", "345693826", "1751649231", "admin"
        );

        assertFalse(result.isSuccess());
        assertEquals("您的角色无权使用此指令", result.getReason());
    }

    @Test
    @DisplayName("测试权限校验 - System指令群主可调用")
    void testCheckPermission_SystemCommand_OwnerAllowed() {
        // System 级别的指令，owner 应该可以调用
        testGlobalPerm.setRoleType("System");

        when(redisStore.getGlobalPermission("pig")).thenReturn(Optional.of(testGlobalPerm));
        // 群聊权限不存在，使用指令的 roleType
        when(redisStore.getGroupPermission("345693826", "pig")).thenReturn(Optional.empty());
        when(groupPermRepository.findByGroupIdAndCommandName("345693826", "pig")).thenReturn(Optional.empty());
        // 用户权限自动初始化
        when(redisStore.getUserPermission("1751649231")).thenReturn(Optional.empty());
        when(userPermRepository.findByUserId("1751649231")).thenReturn(Optional.empty());
        lenient().when(userPermRepository.save(any(UserPermission.class))).thenAnswer(inv -> inv.getArgument(0));

        PermissionCheckResult result = permissionManager.checkPermission(
                "pig", "345693826", "1751649231", "owner"
        );

        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("测试权限校验 - 机器人管理员权限")
    void testCheckPermission_BotAdmin() {
        // 超级管理员拥有最高权限，直接返回成功，不需要检查其他权限
        when(redisStore.isBotAdmin("1751649231")).thenReturn(true);

        PermissionCheckResult result = permissionManager.checkPermission(
                "pig", "345693826", "1751649231", "member"
        );

        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("测试权限校验 - 指令未注册")
    void testCheckPermission_CommandNotFound() {
        when(redisStore.getGlobalPermission("unknown")).thenReturn(Optional.empty());
        when(globalPermRepository.findByCommandName("unknown")).thenReturn(Optional.empty());

        PermissionCheckResult result = permissionManager.checkPermission(
                "unknown", "345693826", "1751649231", "member"
        );

        assertFalse(result.isSuccess());
        assertEquals("指令未注册或已禁用", result.getReason());
    }

    @Test
    @DisplayName("测试获取或初始化群聊权限 - 缓存命中")
    void testGetOrInitGroupPermission_CacheHit() {
        when(redisStore.getGroupPermission("345693826", "pig")).thenReturn(Optional.of(testGroupPerm));

        GroupPermission result = permissionManager.getOrInitGroupPermission("345693826", "pig");

        assertNotNull(result);
        assertEquals("345693826", result.getGroupId());
        assertEquals("pig", result.getCommandName());
        verify(groupPermRepository, never()).findByGroupIdAndCommandName(anyString(), anyString());
    }

    @Test
    @DisplayName("测试获取或初始化群聊权限 - 自动创建默认权限")
    void testGetOrInitGroupPermission_AutoCreate() {
        // 缓存和数据库都未命中，应该自动创建默认权限（默认启用）
        when(redisStore.getGroupPermission("345693826", "pig")).thenReturn(Optional.empty());
        when(groupPermRepository.findByGroupIdAndCommandName("345693826", "pig")).thenReturn(Optional.empty());
        when(groupPermRepository.save(any(GroupPermission.class))).thenAnswer(inv -> inv.getArgument(0));

        GroupPermission result = permissionManager.getOrInitGroupPermission("345693826", "pig");

        assertNotNull(result);
        assertEquals("345693826", result.getGroupId());
        assertEquals("pig", result.getCommandName());
        assertTrue(result.isEnabled());  // 默认启用
        // 验证自动创建并保存
        verify(groupPermRepository).save(any(GroupPermission.class));
        verify(redisStore).saveGroupPermission(any(GroupPermission.class));
    }

    @Test
    @DisplayName("测试获取或初始化用户权限 - 缓存命中")
    void testGetOrInitUserPermission_CacheHit() {
        when(redisStore.getUserPermission("1751649231")).thenReturn(Optional.of(testUserPerm));

        UserPermission result = permissionManager.getOrInitUserPermission("1751649231");

        assertNotNull(result);
        assertEquals("1751649231", result.getUserId());
        verify(userPermRepository, never()).findByUserId(anyString());
    }

    @Test
    @DisplayName("测试获取或初始化用户权限 - 自动创建默认权限")
    void testGetOrInitUserPermission_AutoCreate() {
        // 缓存和数据库都未命中，应该自动创建
        when(redisStore.getUserPermission("1751649231")).thenReturn(Optional.empty());
        when(userPermRepository.findByUserId("1751649231")).thenReturn(Optional.empty());
        when(userPermRepository.save(any(UserPermission.class))).thenAnswer(inv -> inv.getArgument(0));

        UserPermission result = permissionManager.getOrInitUserPermission("1751649231");

        assertNotNull(result);
        assertEquals("1751649231", result.getUserId());
        assertNull(result.getAllowedCommands());  // 默认允许所有指令
        assertFalse(result.isBotAdmin());

        // 验证自动创建并保存
        verify(userPermRepository).save(any(UserPermission.class));
        verify(redisStore).saveUserPermission(any(UserPermission.class));
    }

    @Test
    @DisplayName("测试权限校验 - 无群聊权限时使用指令默认角色限制")
    void testCheckPermission_DefaultRoleFromGlobalPermission() {
        // 全局权限设置为 User 级别（普通成员可以调用）
        testGlobalPerm.setRoleType("User");
        testGlobalPerm.setAllowedRoles(null);  // 使用 roleType 检查

        when(redisStore.getGlobalPermission("pig")).thenReturn(Optional.of(testGlobalPerm));
        // 群聊权限不存在时会自动创建
        when(redisStore.getGroupPermission("345693826", "pig")).thenReturn(Optional.empty());
        when(groupPermRepository.findByGroupIdAndCommandName("345693826", "pig")).thenReturn(Optional.empty());
        when(groupPermRepository.save(any(GroupPermission.class))).thenAnswer(inv -> inv.getArgument(0));
        // 用户权限自动初始化
        when(redisStore.getUserPermission("1751649231")).thenReturn(Optional.empty());
        when(userPermRepository.findByUserId("1751649231")).thenReturn(Optional.empty());
        when(userPermRepository.save(any(UserPermission.class))).thenAnswer(inv -> inv.getArgument(0));

        PermissionCheckResult result = permissionManager.checkPermission(
                "pig", "345693826", "1751649231", "member"
        );

        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("测试权限校验 - 无群聊权限时角色限制生效")
    void testCheckPermission_DefaultRole_Denied() {
        // 全局权限设置为 Admin 级别（普通成员不能调用）
        testGlobalPerm.setRoleType("Admin");
        testGlobalPerm.setAllowedRoles(null);  // 使用 roleType 检查

        when(redisStore.getGlobalPermission("pig")).thenReturn(Optional.of(testGlobalPerm));
        // 群聊权限不存在时会自动创建，但角色检查会先失败
        when(redisStore.getGroupPermission("345693826", "pig")).thenReturn(Optional.empty());
        when(groupPermRepository.findByGroupIdAndCommandName("345693826", "pig")).thenReturn(Optional.empty());
        when(groupPermRepository.save(any(GroupPermission.class))).thenAnswer(inv -> inv.getArgument(0));

        PermissionCheckResult result = permissionManager.checkPermission(
                "pig", "345693826", "1751649231", "member"
        );

        assertFalse(result.isSuccess());
        assertEquals("您的角色无权使用此指令", result.getReason());
    }

    @Test
    @DisplayName("测试权限校验 - 群聊权限覆盖指令角色限制")
    void testCheckPermission_GroupRolesOverride() {
        // 全局权限设置为 User 级别（所有人都可以）
        testGlobalPerm.setRoleType("User");

        when(redisStore.getGlobalPermission("pig")).thenReturn(Optional.of(testGlobalPerm));
        // 群聊权限存在，且只允许 admin
        testGroupPerm.setAllowedRoles(List.of("admin"));
        testGroupPerm.setWhitelist(List.of());  // 移除白名单
        when(redisStore.getGroupPermission("345693826", "pig")).thenReturn(Optional.of(testGroupPerm));
        // 注意：角色检查失败会提前返回，不需要初始化用户权限

        // member 角色被群聊权限限制拒绝
        PermissionCheckResult result = permissionManager.checkPermission(
                "pig", "345693826", "1751649231", "member"
        );

        assertFalse(result.isSuccess());
        assertEquals("您的角色无权使用此指令", result.getReason());
    }

    @Test
    @DisplayName("测试检查机器人管理员")
    void testIsBotAdmin() {
        when(redisStore.isBotAdmin("1751649231")).thenReturn(true);

        boolean result = permissionManager.isBotAdmin("1751649231");

        assertTrue(result);
    }

    @Test
    @DisplayName("测试获取可用指令列表")
    void testGetAvailableCommands() {
        // 设置允许所有角色
        testGlobalPerm.setAllowedRoles(null);

        when(redisStore.getAllGlobalCommands()).thenReturn(Set.of("pig", "chat", "help"));
        when(redisStore.getGlobalPermission(anyString())).thenReturn(Optional.of(testGlobalPerm));
        // 群聊权限不存在
        when(redisStore.getGroupPermission(anyString(), anyString())).thenReturn(Optional.empty());
        when(groupPermRepository.findByGroupIdAndCommandName(anyString(), anyString())).thenReturn(Optional.empty());
        // 用户权限自动初始化
        lenient().when(userPermRepository.save(any(UserPermission.class))).thenAnswer(inv -> inv.getArgument(0));
        when(redisStore.getUserPermission(anyString())).thenReturn(Optional.empty());
        when(userPermRepository.findByUserId(anyString())).thenReturn(Optional.empty());

        List<String> result = permissionManager.getAvailableCommands(
                "345693826", "1751649231", "member"
        );

        assertNotNull(result);
        assertEquals(3, result.size());
    }
}
