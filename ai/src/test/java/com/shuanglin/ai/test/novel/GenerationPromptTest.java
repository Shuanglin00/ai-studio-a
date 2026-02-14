package com.shuanglin.ai.test.novel;

import com.shuanglin.ai.novel.service.GenerationPrompt;
import com.shuanglin.ai.novel.service.GenerationPrompt.ChapterPrompt;
import com.shuanglin.ai.novel.service.GenerationPrompt.GenerationContext;
import com.shuanglin.ai.novel.service.GenerationPrompt.CharacterState;
import com.shuanglin.ai.novel.service.GenerationPrompt.HistoricalEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GenerationPrompt 单元测试
 * 测试生成提示模板的构建逻辑
 */
@DisplayName("GenerationPrompt 测试")
class GenerationPromptTest {

    private GenerationPrompt generationPrompt;

    @BeforeEach
    void setUp() {
        generationPrompt = new GenerationPrompt();
    }

    @Test
    @DisplayName("测试续写模式系统提示构建")
    void testBuildContinueSystemPrompt() {
        // 准备测试数据
        GenerationContext context = createTestContext();

        // 执行
        String result = generationPrompt.buildContinueSystemPrompt(context);

        // 验证
        assertNotNull(result);
        assertTrue(result.contains("小说续写任务"));
        assertTrue(result.contains("测试小说"));
        assertTrue(result.contains("传统玄幻"));
        assertTrue(result.contains("主角"));
        assertTrue(result.contains("化神期"));
    }

    @Test
    @DisplayName("测试概要模式系统提示构建")
    void testBuildOutlineSystemPrompt() {
        // 准备测试数据
        GenerationContext context = createTestContext();
        String outline = "主角前往青云山参加宗门大比";

        // 执行
        String result = generationPrompt.buildOutlineSystemPrompt(context, outline);

        // 验证
        assertNotNull(result);
        assertTrue(result.contains("小说生成任务"));
        assertTrue(result.contains("概要模式"));
        assertTrue(result.contains(outline));
    }

    @Test
    @DisplayName("测试混合模式系统提示构建")
    void testBuildHybridSystemPrompt() {
        // 准备测试数据
        GenerationContext context = createTestContext();
        String content = "主角正在青云山修炼...";
        String outline = "遇到强敌，发生战斗";

        // 执行
        String result = generationPrompt.buildHybridSystemPrompt(context, content, outline);

        // 验证
        assertNotNull(result);
        assertTrue(result.contains("混合模式"));
        assertTrue(result.contains(content));
        assertTrue(result.contains(outline));
    }

    @Test
    @DisplayName("测试续写用户提示构建")
    void testBuildContinueUserPrompt() {
        // 准备测试数据
        GenerationContext context = createTestContext();
        ChapterPrompt prompt = new ChapterPrompt();
        prompt.setRecentChaptersSummary("上一章讲到主角突破了境界");
        prompt.setChapterTitle("第101章 新的挑战");
        prompt.setChapterLength(5000);

        // 执行
        String result = generationPrompt.buildContinueUserPrompt(context, prompt);

        // 验证
        assertNotNull(result);
        assertTrue(result.contains("最近剧情"));
        assertTrue(result.contains("上一章讲到主角突破了境界"));
        assertTrue(result.contains("章节标题"));
        assertTrue(result.contains("第101章 新的挑战"));
        assertTrue(result.contains("期望长度"));
        assertTrue(result.contains("5000"));
    }

    @Test
    @DisplayName("测试概要用户提示构建")
    void testBuildOutlineUserPrompt() {
        // 准备测试数据
        GenerationContext context = createTestContext();
        ChapterPrompt prompt = new ChapterPrompt();
        prompt.setChapterLength(3000);
        String outline = "主角前往遗迹探险";

        // 执行
        String result = generationPrompt.buildOutlineUserPrompt(context, prompt, outline);

        // 验证
        assertNotNull(result);
        assertTrue(result.contains("情节概要"));
        assertTrue(result.contains(outline));
        assertTrue(result.contains("期望长度"));
    }

    @Test
    @DisplayName("测试空上下文处理")
    void testBuildPromptWithEmptyContext() {
        // 准备空上下文
        GenerationContext context = new GenerationContext();
        context.setBookName("");

        // 执行
        String result = generationPrompt.buildContinueSystemPrompt(context);

        // 验证 - 应该能处理空值
        assertNotNull(result);
    }

    @Test
    @DisplayName("测试ChapterPrompt变量管理")
    void testChapterPromptVariables() {
        // 准备
        ChapterPrompt prompt = new ChapterPrompt();
        prompt.addVariable("key1", "value1");
        prompt.addVariable("key2", "value2");

        // 执行 & 验证
        assertEquals("value1", prompt.getVariable("key1"));
        assertEquals("value2", prompt.getVariable("key2"));
        assertNull(prompt.getVariable("nonexistent"));
    }

    @Test
    @DisplayName("测试GenerationContext数据封装")
    void testGenerationContextData() {
        // 准备 & 执行
        GenerationContext context = new GenerationContext();
        context.setBookUuid("test-uuid");
        context.setBookName("测试书籍");
        context.setWritingStyle("仙侠");

        // 验证
        assertEquals("test-uuid", context.getBookUuid());
        assertEquals("测试书籍", context.getBookName());
        assertEquals("仙侠", context.getWritingStyle());
    }

    // 辅助方法：创建测试用GenerationContext
    private GenerationContext createTestContext() {
        GenerationContext context = new GenerationContext();
        context.setBookUuid("test-001");
        context.setBookName("测试小说");
        context.setWritingStyle("传统玄幻");

        // 人物状态
        List<CharacterState> states = new ArrayList<>();
        CharacterState state = new CharacterState();
        state.setCharacterName("主角");
        state.setRealm("化神期");
        state.setLocation("青云山");
        state.setChapterIndex(10);
        states.add(state);
        context.setCharacterStates(states);

        // 历史事件
        List<HistoricalEvent> events = new ArrayList<>();
        HistoricalEvent event = new HistoricalEvent();
        event.setEventUuid("evt-001");
        event.setChapterIndex(5);
        event.setEventType("BREAKTHROUGH");
        event.setDescription("主角突破到化神期");
        events.add(event);
        context.setHistoricalEvents(events);

        return context;
    }
}
