package com.shuanglin.framework.permission;

import com.shuanglin.common.enums.CommandType;
import com.shuanglin.common.enums.PermissionLevel;
import com.shuanglin.dao.bot.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 数据库交互测试（MongoDB）
 */
@DataMongoTest
@Import(TestMongoConfig.class)
class DatabaseInteractionTest {

    @Autowired
    private GlobalPermissionRepository globalPermRepository;

    @Autowired
    private GroupPermissionRepository groupPermRepository;

    @Autowired
    private UserPermissionRepository userPermRepository;

    private GlobalPermission testGlobalPerm;
    private GroupPermission testGroupPerm;
    private UserPermission testUserPerm;

    @BeforeEach
    void setUp() {
        // 清理数据
        globalPermRepository.deleteAll();
        groupPermRepository.deleteAll();
        userPermRepository.deleteAll();

        // 创建测试数据
        testGlobalPerm = GlobalPermission.builder()
                .commandName("test_cmd")
                .triggerPrefix("!test")
                .commandType(CommandType.GROUP)
                .level(PermissionLevel.PUBLIC)
                .enabled(true)
                .allowedRoles(List.of("default"))
                .globalBlacklist(List.of())
                .globalWhitelist(List.of())
                .description("测试指令")
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        testGroupPerm = GroupPermission.builder()
                .groupId("test_group_123")
                .commandName("test_cmd")
                .commandType(CommandType.GROUP)
                .enabled(true)
                .blacklist(List.of())
                .whitelist(List.of("user_1"))
                .allowedRoles(List.of("default"))
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        testUserPerm = UserPermission.builder()
                .userId("test_user_123")
                .allowedCommands(List.of("cmd1", "cmd2"))
                .deniedCommands(List.of())
                .botAdmin(false)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("测试全局权限 CRUD")
    void testGlobalPermissionCrud() {
        // Create
        GlobalPermission saved = globalPermRepository.save(testGlobalPerm);
        assertNotNull(saved.getId());
        assertEquals("test_cmd", saved.getCommandName());

        // Read
        Optional<GlobalPermission> found = globalPermRepository.findByCommandName("test_cmd");
        assertTrue(found.isPresent());
        assertEquals(CommandType.GROUP, found.get().getCommandType());

        // Update
        saved.setEnabled(false);
        saved.setUpdateTime(LocalDateTime.now());
        GlobalPermission updated = globalPermRepository.save(saved);
        assertFalse(updated.isEnabled());

        // Delete
        globalPermRepository.delete(saved);
        Optional<GlobalPermission> deleted = globalPermRepository.findByCommandName("test_cmd");
        assertFalse(deleted.isPresent());
    }

    @Test
    @DisplayName("测试群聊权限 CRUD")
    void testGroupPermissionCrud() {
        // Create
        GroupPermission saved = groupPermRepository.save(testGroupPerm);
        assertNotNull(saved.getId());
        assertEquals("test_group_123", saved.getGroupId());
        assertEquals("test_cmd", saved.getCommandName());

        // Read
        Optional<GroupPermission> found = groupPermRepository.findByGroupIdAndCommandName(
                "test_group_123", "test_cmd");
        assertTrue(found.isPresent());
        assertEquals("user_1", found.get().getWhitelist().get(0));

        // Get all by group
        List<GroupPermission> groupPerms = groupPermRepository.findByGroupId("test_group_123");
        assertEquals(1, groupPerms.size());

        // Delete
        groupPermRepository.deleteByGroupIdAndCommandName("test_group_123", "test_cmd");
        Optional<GroupPermission> deleted = groupPermRepository.findByGroupIdAndCommandName(
                "test_group_123", "test_cmd");
        assertFalse(deleted.isPresent());
    }

    @Test
    @DisplayName("测试用户权限 CRUD")
    void testUserPermissionCrud() {
        // Create
        UserPermission saved = userPermRepository.save(testUserPerm);
        assertNotNull(saved.getId());
        assertEquals("test_user_123", saved.getUserId());

        // Read
        Optional<UserPermission> found = userPermRepository.findByUserId("test_user_123");
        assertTrue(found.isPresent());
        assertEquals(2, found.get().getAllowedCommands().size());
        assertFalse(found.get().isBotAdmin());

        // Update admin status
        saved.setBotAdmin(true);
        saved.setUpdateTime(LocalDateTime.now());
        UserPermission updated = userPermRepository.save(saved);
        assertTrue(updated.isBotAdmin());

        // Find all admins
        List<UserPermission> admins = userPermRepository.findByBotAdminTrue();
        assertTrue(admins.stream().anyMatch(u -> u.getUserId().equals("test_user_123")));

        // Delete
        userPermRepository.delete(saved);
        Optional<UserPermission> deleted = userPermRepository.findByUserId("test_user_123");
        assertFalse(deleted.isPresent());
    }

    @Test
    @DisplayName("测试批量查询")
    void testBatchQuery() {
        // 保存多个权限
        globalPermRepository.save(testGlobalPerm);
        globalPermRepository.save(GlobalPermission.builder()
                .commandName("cmd2")
                .triggerPrefix("!cmd2")
                .commandType(CommandType.GLOBAL)
                .level(PermissionLevel.PUBLIC)
                .enabled(true)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build());

        // 查询所有启用的全局权限
        List<GlobalPermission> enabledPerms = globalPermRepository.findByEnabled(true);
        assertEquals(2, enabledPerms.size());

        // 按类型查询
        List<GlobalPermission> globalPerms = globalPermRepository.findByCommandTypeAndEnabled(
                CommandType.GLOBAL, true);
        assertEquals(1, globalPerms.size());
    }

    @Test
    @DisplayName("测试复合索引")
    void testCompoundIndex() {
        // 创建两个不同群的相同指令权限
        GroupPermission perm1 = GroupPermission.builder()
                .groupId("group_1")
                .commandName("shared_cmd")
                .commandType(CommandType.GROUP)
                .enabled(true)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        GroupPermission perm2 = GroupPermission.builder()
                .groupId("group_2")
                .commandName("shared_cmd")
                .commandType(CommandType.GROUP)
                .enabled(true)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        groupPermRepository.save(perm1);
        groupPermRepository.save(perm2);

        // 分别查询
        Optional<GroupPermission> found1 = groupPermRepository.findByGroupIdAndCommandName(
                "group_1", "shared_cmd");
        Optional<GroupPermission> found2 = groupPermRepository.findByGroupIdAndCommandName(
                "group_2", "shared_cmd");

        assertTrue(found1.isPresent());
        assertTrue(found2.isPresent());
        assertEquals("group_1", found1.get().getGroupId());
        assertEquals("group_2", found2.get().getGroupId());
    }
}
