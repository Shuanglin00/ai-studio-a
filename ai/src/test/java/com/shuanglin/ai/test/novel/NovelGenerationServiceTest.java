package com.shuanglin.ai.test.novel;

import com.shuanglin.ai.langchain4j.assistant.DecomposeAssistant;
import com.shuanglin.ai.novel.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * NovelGenerationService 单元测试
 * 测试小说生成服务的核心方法
 */
@DisplayName("NovelGenerationService 测试")
@ExtendWith(MockitoExtension.class)
class NovelGenerationServiceTest {

    @Mock
    private DecomposeAssistant decomposeAssistant;

    @Mock
    private GraphRagService graphRagService;

    @Mock
    private NovelEntityService novelEntityService;

    @Mock
    private GenerationPrompt generationPrompt;

    @InjectMocks
    private NovelGenerationService novelGenerationService;

    @BeforeEach
    void setUp() {
        // 设置
    }

    @Test
    @DisplayName("测试人物状态信息创建")
    void testCharacterStateInfoCreation() {
        // 创建人物状态信息
        NovelGenerationService.CharacterStateInfo info = new NovelGenerationService.CharacterStateInfo();

        info.setCharacterName("主角");
        info.setCurrentStates(new ArrayList<>());
        info.setRecentEvents(new ArrayList<>());

        // 验证
        assertEquals("主角", info.getCharacterName());
        assertNotNull(info.getCurrentStates());
        assertNotNull(info.getRecentEvents());
    }

    @Test
    @DisplayName("测试GenerationPrompt初始化")
    void testGenerationPromptInitialization() {
        // 验证GenerationPrompt能够正常创建
        GenerationPrompt prompt = new GenerationPrompt();
        assertNotNull(prompt);
    }

    @Test
    @DisplayName("测试GenerationContext创建")
    void testGenerationContextCreation() {
        // 创建GenerationContext
        GenerationPrompt.GenerationContext context = new GenerationPrompt.GenerationContext();
        context.setBookUuid("book-001");
        context.setBookName("测试小说");
        context.setWritingStyle("玄幻");

        // 设置人物状态
        List<GenerationPrompt.CharacterState> states = new ArrayList<>();
        GenerationPrompt.CharacterState state = new GenerationPrompt.CharacterState();
        state.setCharacterName("主角");
        state.setRealm("化神期");
        states.add(state);
        context.setCharacterStates(states);

        // 设置历史事件
        List<GenerationPrompt.HistoricalEvent> events = new ArrayList<>();
        GenerationPrompt.HistoricalEvent event = new GenerationPrompt.HistoricalEvent();
        event.setChapterIndex(5);
        event.setEventType("BREAKTHROUGH");
        events.add(event);
        context.setHistoricalEvents(events);

        // 验证
        assertEquals("book-001", context.getBookUuid());
        assertEquals("测试小说", context.getBookName());
        assertEquals("玄幻", context.getWritingStyle());
        assertEquals(1, context.getCharacterStates().size());
        assertEquals(1, context.getHistoricalEvents().size());
    }

    @Test
    @DisplayName("测试ChapterPrompt创建")
    void testChapterPromptCreation() {
        // 创建ChapterPrompt
        GenerationPrompt.ChapterPrompt prompt = new GenerationPrompt.ChapterPrompt();

        prompt.setRecentChaptersSummary("上一章讲到...");
        prompt.setChapterTitle("第100章 新的开始");
        prompt.setChapterLength(3000);

        // 测试变量添加
        prompt.addVariable("key1", "value1");
        prompt.addVariable("key2", "value2");

        // 验证
        assertEquals("上一章讲到...", prompt.getRecentChaptersSummary());
        assertEquals("第100章 新的开始", prompt.getChapterTitle());
        assertEquals(3000, prompt.getChapterLength());
        assertEquals("value1", prompt.getVariable("key1"));
        assertEquals("value2", prompt.getVariable("key2"));
    }

    @Test
    @DisplayName("测试CharacterState数据封装")
    void testCharacterStateData() {
        // 创建CharacterState
        GenerationPrompt.CharacterState state = new GenerationPrompt.CharacterState();
        state.setCharacterName("萧炎");
        state.setRealm("斗帝");
        state.setLocation("斗气大陆");
        state.setHealth("最佳");
        state.setEmotion("平静");
        state.setChapterIndex(100);

        // 验证
        assertEquals("萧炎", state.getCharacterName());
        assertEquals("斗帝", state.getRealm());
        assertEquals("斗气大陆", state.getLocation());
        assertEquals("最佳", state.getHealth());
        assertEquals("平静", state.getEmotion());
        assertEquals(100, state.getChapterIndex());
    }

    @Test
    @DisplayName("测试HistoricalEvent数据封装")
    void testHistoricalEventData() {
        // 创建HistoricalEvent
        GenerationPrompt.HistoricalEvent event = new GenerationPrompt.HistoricalEvent();
        event.setEventUuid("evt-001");
        event.setChapterIndex(50);
        event.setEventType("BATTLE");
        event.setDescription("萧炎与魂天帝的最终决战");

        List<String> participants = new ArrayList<>();
        participants.add("萧炎");
        participants.add("魂天帝");
        event.setParticipants(participants);

        // 验证
        assertEquals("evt-001", event.getEventUuid());
        assertEquals(50, event.getChapterIndex());
        assertEquals("BATTLE", event.getEventType());
        assertEquals("萧炎与魂天帝的最终决战", event.getDescription());
        assertEquals(2, event.getParticipants().size());
    }

    @Test
    @DisplayName("测试LocationInfo数据封装")
    void testLocationInfoData() {
        // 创建LocationInfo
        GenerationPrompt.LocationInfo location = new GenerationPrompt.LocationInfo();
        location.setLocationName("青云山");
        location.setDescription("青云宗所在地");

        List<String> connected = new ArrayList<>();
        connected.add("青云城");
        connected.add("迷雾森林");
        location.setConnectedLocations(connected);

        // 验证
        assertEquals("青云山", location.getLocationName());
        assertEquals("青云宗所在地", location.getDescription());
        assertEquals(2, location.getConnectedLocations().size());
    }

    @Test
    @DisplayName("测试服务初始化")
    void testServiceInitialization() {
        // 验证服务能够正常初始化
        assertNotNull(novelGenerationService);
    }
}
