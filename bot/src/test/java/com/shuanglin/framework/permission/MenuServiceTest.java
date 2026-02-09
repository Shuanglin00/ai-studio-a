package com.shuanglin.framework.permission;

import com.shuanglin.common.enums.CommandType;
import com.shuanglin.dao.bot.GlobalPermission;
import com.shuanglin.framework.command.CommandInfo;
import com.shuanglin.framework.enums.RoleType;
import com.shuanglin.framework.registry.CommandRegistry;
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
 * 菜单服务测试
 */
@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock
    private CommandRegistry commandRegistry;

    @Mock
    private PermissionManager permissionManager;

    @InjectMocks
    private MenuService menuService;

    private List<CommandInfo> testCommands;

    @BeforeEach
    void setUp() {
        testCommands = Arrays.asList(
                CommandInfo.builder()
                        .commandName("help")
                        .triggerPrefix("!help")
                        .role(RoleType.User)
                        .requireAdmin(false)
                        .description("显示帮助")
                        .build(),
                CommandInfo.builder()
                        .commandName("pig")
                        .triggerPrefix("渚")
                        .role(RoleType.User)
                        .requireAdmin(false)
                        .description("发送猪图片")
                        .build(),
                CommandInfo.builder()
                        .commandName("admin")
                        .triggerPrefix("!admin")
                        .role(RoleType.Admin)
                        .requireAdmin(true)
                        .description("管理功能")
                        .build()
        );
    }

    @Test
    @DisplayName("测试获取全局指令")
    void testGetGlobalCommands() {
        when(commandRegistry.getAllCommands()).thenReturn(testCommands);

        // help 和 admin 都是 ! 开头，detectCommandType 会返回 GLOBAL
        GlobalPermission helpPerm = GlobalPermission.builder()
                .commandName("help")
                .commandType(CommandType.GLOBAL)
                .build();
        GlobalPermission pigPerm = GlobalPermission.builder()
                .commandName("pig")
                .commandType(CommandType.GROUP)
                .build();
        GlobalPermission adminPerm = GlobalPermission.builder()
                .commandName("admin")
                .commandType(CommandType.GLOBAL)
                .build();

        when(permissionManager.getGlobalPermission("help")).thenReturn(Optional.of(helpPerm));
        when(permissionManager.getGlobalPermission("pig")).thenReturn(Optional.of(pigPerm));
        when(permissionManager.getGlobalPermission("admin")).thenReturn(Optional.of(adminPerm));

        List<CommandItem> result = menuService.getGlobalCommands();

        assertEquals(2, result.size()); // help 和 admin
        assertTrue(result.stream().anyMatch(c -> "help".equals(c.getName())));
        assertTrue(result.stream().anyMatch(c -> "admin".equals(c.getName())));
    }

    @Test
    @DisplayName("测试获取群聊指令")
    void testGetGroupCommands() {
        when(commandRegistry.getAllCommands()).thenReturn(testCommands);

        GlobalPermission helpPerm = GlobalPermission.builder()
                .commandName("help")
                .commandType(CommandType.GLOBAL)
                .build();
        GlobalPermission pigPerm = GlobalPermission.builder()
                .commandName("pig")
                .commandType(CommandType.GROUP)
                .build();
        GlobalPermission adminPerm = GlobalPermission.builder()
                .commandName("admin")
                .commandType(CommandType.GLOBAL)
                .build();

        when(permissionManager.getGlobalPermission("help")).thenReturn(Optional.of(helpPerm));
        when(permissionManager.getGlobalPermission("pig")).thenReturn(Optional.of(pigPerm));
        when(permissionManager.getGlobalPermission("admin")).thenReturn(Optional.of(adminPerm));

        List<CommandItem> result = menuService.getGroupCommands();

        assertEquals(1, result.size()); // 只有 pig 是 GROUP
        assertTrue(result.stream().anyMatch(c -> "pig".equals(c.getName())));
    }

    @Test
    @DisplayName("测试获取完整菜单")
    void testGetFullMenu() {
        when(commandRegistry.getAllCommands()).thenReturn(testCommands);

        GlobalPermission helpPerm = GlobalPermission.builder()
                .commandName("help")
                .commandType(CommandType.GLOBAL)
                .build();
        GlobalPermission pigPerm = GlobalPermission.builder()
                .commandName("pig")
                .commandType(CommandType.GROUP)
                .build();

        when(permissionManager.getGlobalPermission("help")).thenReturn(Optional.of(helpPerm));
        when(permissionManager.getGlobalPermission("pig")).thenReturn(Optional.of(pigPerm));
        when(permissionManager.getGlobalPermission("admin")).thenReturn(Optional.of(helpPerm));

        CommandMenu menu = menuService.getFullMenu();

        assertNotNull(menu);
        assertFalse(menu.getGlobalCommands().isEmpty());
        assertFalse(menu.getGroupCommands().isEmpty());
    }

    @Test
    @DisplayName("测试获取可用菜单")
    void testGetAvailableMenu() {
        when(commandRegistry.getAllCommands()).thenReturn(testCommands);
        when(permissionManager.getAvailableCommands("345693826", "1751649231", "member"))
                .thenReturn(Arrays.asList("help", "pig"));

        GlobalPermission helpPerm = GlobalPermission.builder()
                .commandName("help")
                .commandType(CommandType.GLOBAL)
                .build();
        GlobalPermission pigPerm = GlobalPermission.builder()
                .commandName("pig")
                .commandType(CommandType.GROUP)
                .build();

        when(permissionManager.getGlobalPermission("help")).thenReturn(Optional.of(helpPerm));
        when(permissionManager.getGlobalPermission("pig")).thenReturn(Optional.of(pigPerm));
        when(permissionManager.getGlobalPermission("admin")).thenReturn(Optional.of(pigPerm));

        CommandMenu menu = menuService.getAvailableMenu("345693826", "1751649231", "member");

        assertEquals(1, menu.getGlobalCommands().size());
        assertEquals(1, menu.getGroupCommands().size());
        assertEquals(0, menu.getPrivateCommands().size());
    }

    @Test
    @DisplayName("测试生成菜单文本")
    void testBuildMenuText() {
        when(commandRegistry.getAllCommands()).thenReturn(testCommands);
        when(permissionManager.getAvailableCommands(anyString(), anyString(), anyString()))
                .thenReturn(Arrays.asList("help", "pig"));

        GlobalPermission helpPerm = GlobalPermission.builder()
                .commandName("help")
                .commandType(CommandType.GLOBAL)
                .build();
        GlobalPermission pigPerm = GlobalPermission.builder()
                .commandName("pig")
                .commandType(CommandType.GROUP)
                .build();

        when(permissionManager.getGlobalPermission("help")).thenReturn(Optional.of(helpPerm));
        when(permissionManager.getGlobalPermission("pig")).thenReturn(Optional.of(pigPerm));
        when(permissionManager.getGlobalPermission("admin")).thenReturn(Optional.of(pigPerm));

        String menuText = menuService.buildMenuText(
                menuService.getAvailableMenu("345693826", "1751649231", "member"),
                "345693826",
                "1751649231",
                "member"
        );

        assertNotNull(menuText);
        assertTrue(menuText.contains("指令菜单"));
        assertTrue(menuText.contains("!help"));
        assertTrue(menuText.contains("渚"));
    }

    @Test
    @DisplayName("测试根据前缀查找指令")
    void testGetCommandByPrefix() {
        when(commandRegistry.getCommandByPrefix("渚")).thenReturn(testCommands.get(1));

        Optional<CommandItem> result = menuService.getCommandByPrefix("渚");

        assertTrue(result.isPresent());
        assertEquals("pig", result.get().getName());
    }

    @Test
    @DisplayName("测试获取指令详情")
    void testGetCommandDetail() {
        when(commandRegistry.getCommandByName("help")).thenReturn(testCommands.get(0));

        Optional<CommandItem> result = menuService.getCommandDetail("help");

        assertTrue(result.isPresent());
        assertEquals("help", result.get().getName());
        assertEquals("显示帮助", result.get().getDescription());
    }
}
