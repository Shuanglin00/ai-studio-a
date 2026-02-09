package com.shuanglin.framework.permission;

import com.shuanglin.framework.bus.event.GroupMessageEvent;
import com.shuanglin.framework.bus.event.data.Sender;
import com.shuanglin.framework.command.CommandInfo;
import com.shuanglin.framework.enums.RoleType;
import com.shuanglin.framework.enums.onebot.GroupRole;
import com.shuanglin.framework.enums.onebot.MessageType;
import com.shuanglin.framework.enums.onebot.Sex;
import com.shuanglin.framework.enums.onebot.SubType;
import com.shuanglin.framework.onebot.segment.MessageSegment;
import com.shuanglin.framework.onebot.segment.TextSegment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 权限校验器测试
 */
@ExtendWith(MockitoExtension.class)
class PermissionValidatorTest {

    @Mock
    private PermissionManager permissionManager;

    @InjectMocks
    private PermissionValidator permissionValidator;

    private GroupMessageEvent testEvent;
    private CommandInfo testCommand;
    private Sender testSender;
    private List<MessageSegment> testMessageSegments;

    @BeforeEach
    void setUp() {
        // 创建测试发送者
        testSender = new Sender();
        testSender.setUserId(1751649231L);
        testSender.setNickname("测试用户");
        testSender.setRole(GroupRole.MEMBER);
        testSender.setCard("");
        testSender.setTitle("");
        testSender.setSex(Sex.MALE);
        testSender.setAge(25);

        // 创建测试消息段
        testMessageSegments = new ArrayList<>();
        testMessageSegments.add(new TextSegment("渚"));

        // 创建测试事件
        testEvent = new GroupMessageEvent();
        testEvent.setUserId(1751649231L);
        testEvent.setGroupId("345693826");
        testEvent.setMessage(testMessageSegments);
        testEvent.setRawMessage("渚");
        testEvent.setMessageId(123456L);
        testEvent.setMessageType(MessageType.GROUP);
        testEvent.setSubType(SubType.NORMAL);
        testEvent.setSender(testSender);

        // 创建测试指令
        testCommand = CommandInfo.builder()
                .commandName("pig_group_message")
                .triggerPrefix("渚")
                .role(RoleType.User)
                .requireAdmin(false)
                .description("发送猪图片")
                .build();
    }

    @Test
    @DisplayName("测试管理员权限校验 - 群主应该有管理员权限")
    void testAdminPermission_Owner() {
        testSender.setRole(GroupRole.OWNER);

        Boolean result = permissionValidator.checkAdminPermission(testEvent, "345693826", "1751649231");

        assertTrue(result);
    }

    @Test
    @DisplayName("测试管理员权限校验 - 管理员应该有管理员权限")
    void testAdminPermission_Admin() {
        testSender.setRole(GroupRole.ADMIN);

        Boolean result = permissionValidator.checkAdminPermission(testEvent, "345693826", "1751649231");

        assertTrue(result);
    }

    @Test
    @DisplayName("测试管理员权限校验 - 普通成员无管理员权限")
    void testAdminPermission_Member() {
        testSender.setRole(GroupRole.MEMBER);
        when(permissionManager.isBotAdmin(anyString())).thenReturn(false);

        Boolean result = permissionValidator.checkAdminPermission(testEvent, "345693826", "1751649231");

        assertFalse(result);
    }

    @Test
    @DisplayName("测试管理员权限校验 - 机器人管理员应有权限")
    void testAdminPermission_BotAdmin() {
        testSender.setRole(GroupRole.MEMBER);
        when(permissionManager.isBotAdmin("1751649231")).thenReturn(true);

        Boolean result = permissionValidator.checkAdminPermission(testEvent, "345693826", "1751649231");

        assertTrue(result);
    }

    @Test
    @DisplayName("测试权限校验 - 权限校验通过")
    void testValidate_Success() {
        PermissionCheckResult permResult = PermissionCheckResult.success();
        when(permissionManager.checkPermission(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(permResult);

        ValidationResult result = permissionValidator.validate(testEvent, testCommand);

        assertTrue(result.isSuccess());
        verify(permissionManager).checkPermission(
                "pig_group_message",
                "345693826",
                "1751649231",
                "member"
        );
    }

    @Test
    @DisplayName("测试权限校验 - 权限校验失败")
    void testValidate_PermissionDenied() {
        PermissionCheckResult permResult = PermissionCheckResult.fail("您已被禁止使用此指令");
        when(permissionManager.checkPermission(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(permResult);

        ValidationResult result = permissionValidator.validate(testEvent, testCommand);

        assertFalse(result.isSuccess());
        assertEquals("您已被禁止使用此指令", result.getReason());
    }

    @Test
    @DisplayName("测试权限校验 - 角色获取默认值")
    void testValidate_DefaultRole() {
        testSender.setRole(null);

        PermissionCheckResult permResult = PermissionCheckResult.success();
        when(permissionManager.checkPermission(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(permResult);

        ValidationResult result = permissionValidator.validate(testEvent, testCommand);

        assertTrue(result.isSuccess());
        verify(permissionManager).checkPermission(
                anyString(),
                anyString(),
                anyString(),
                eq("member") // 默认角色
        );
    }
}
