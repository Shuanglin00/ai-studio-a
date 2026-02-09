package com.shuanglin.framework.registry;

import com.shuanglin.dao.bot.CommandRepository;
import com.shuanglin.framework.annotation.GroupMessageHandler;
import com.shuanglin.framework.command.CommandInfo;
import com.shuanglin.framework.enums.RoleType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 指令注册表测试
 */
@SpringBootTest(classes = {CommandRegistry.class, CommandRegistryTest.TestExecutor.class})
class CommandRegistryTest {

    @Autowired
    private CommandRegistry commandRegistry;

    @Autowired
    private ApplicationContext applicationContext;

    @MockBean
    private CommandRepository commandRepository;

    @Test
    @DisplayName("测试指令注册")
    void testCommandRegistration() {
        // 手动触发 ContextRefreshedEvent
        ContextRefreshedEvent event = new ContextRefreshedEvent(applicationContext);
        commandRegistry.onApplicationEvent(event);

        List<CommandInfo> commands = commandRegistry.getAllCommands();

        assertFalse(commands.isEmpty());
        assertTrue(commands.stream()
                .anyMatch(c -> "test_pig".equals(c.getCommandName())));
        assertTrue(commands.stream()
                .anyMatch(c -> "渚".equals(c.getTriggerPrefix())));
    }

    @Test
    @DisplayName("测试根据前缀查找指令")
    void testGetCommandByPrefix() {
        ContextRefreshedEvent event = new ContextRefreshedEvent(applicationContext);
        commandRegistry.onApplicationEvent(event);

        CommandInfo command = commandRegistry.getCommandByPrefix("渚");

        assertNotNull(command);
        assertEquals("渚", command.getTriggerPrefix());
        assertEquals("test_pig", command.getCommandName());
    }

    @Test
    @DisplayName("测试根据名称查找指令")
    void testGetCommandByName() {
        ContextRefreshedEvent event = new ContextRefreshedEvent(applicationContext);
        commandRegistry.onApplicationEvent(event);

        CommandInfo command = commandRegistry.getCommandByName("test_pig");

        assertNotNull(command);
        assertEquals("test_pig", command.getCommandName());
    }

    @Test
    @DisplayName("测试获取所有指令")
    void testGetAllCommands() {
        ContextRefreshedEvent event = new ContextRefreshedEvent(applicationContext);
        commandRegistry.onApplicationEvent(event);

        List<CommandInfo> commands = commandRegistry.getAllCommands();

        assertNotNull(commands);
        assertFalse(commands.isEmpty());
    }

    /**
     * 测试用的执行器
     */
    @Slf4j
    @org.springframework.stereotype.Component
    static class TestExecutor {

        @GroupMessageHandler(
                triggerPrefix = "渚",
                role = RoleType.User,
                requireAdmin = false,
                description = "测试猪指令"
        )
        public void testPig(com.shuanglin.framework.bus.event.GroupMessageEvent event) {
            log.info("Test pig command executed");
        }
    }
}
