package com.shuanglin.ai.novel.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shuanglin.ai.langchain4j.assistant.DecomposeAssistant;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 小说生成服务
 * 负责阶段四的推理式RAG和章节生成
 */
@Slf4j
@Service
public class NovelGenerationService {

    private static final String NEO4J_URI = "bolt://8.138.204.38:7687";
    private static final String NEO4J_USER = "neo4j";
    private static final String NEO4J_PASSWORD = "password";

    @Autowired
    private DecomposeAssistant decomposeAssistant;

    @Autowired
    private GraphRagService graphRagService;

    @Autowired
    private NovelEntityService novelEntityService;

    @Autowired
    private GenerationPrompt generationPrompt;

    private final Driver driver;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NovelGenerationService() {
        this.driver = GraphDatabase.driver(NEO4J_URI, AuthTokens.basic(NEO4J_USER, NEO4J_PASSWORD));
    }

    /**
     * 推理式RAG检索
     * @param bookUuid 书籍UUID
     * @param query 查询内容
     * @return 生成上下文
     */
    public GenerationPrompt.GenerationContext retrieveContext(String bookUuid, String query) {
        log.info("开始推理式RAG检索，书籍: {}, 查询: {}", bookUuid, query);

        GenerationPrompt.GenerationContext context = new GenerationPrompt.GenerationContext();
        context.setBookUuid(bookUuid);

        // 1. 从知识图谱查询相关实体
        List<String> relatedEntities = queryRelatedEntities(bookUuid, query);
        log.debug("查询到{}个相关实体", relatedEntities.size());

        // 2. 查询实体当前状态
        List<GenerationPrompt.CharacterState> characterStates = new ArrayList<>();
        for (String entityName : relatedEntities) {
            List<Map<String, Object>> states = graphRagService.getEntityCurrentState(bookUuid, entityName);
            for (Map<String, Object> state : states) {
                GenerationPrompt.CharacterState cs = new GenerationPrompt.CharacterState();
                cs.setCharacterName(entityName);
                cs.setRealm((String) state.get("stateType"));
                cs.setLocation((String) state.get("stateValue"));
                characterStates.add(cs);
            }
        }
        context.setCharacterStates(characterStates);

        // 3. 查询相关事件
        List<GenerationPrompt.HistoricalEvent> events = new ArrayList<>();
        for (String entityName : relatedEntities) {
            List<Map<String, Object>> history = graphRagService.getEntityHistoryEvents(bookUuid, entityName);
            for (Map<String, Object> h : history) {
                GenerationPrompt.HistoricalEvent he = new GenerationPrompt.HistoricalEvent();
                he.setEventUuid((String) h.get("chapterIndex"));
                he.setChapterIndex((Integer) h.get("chapterIndex"));
                he.setEventType((String) h.get("eventType"));
                he.setDescription((String) h.get("description"));
                events.add(he);
            }
        }
        context.setHistoricalEvents(events);

        log.info("RAG检索完成，上下文包含{}个人物状态，{}个历史事件",
                characterStates.size(), events.size());

        return context;
    }

    /**
     * 续写模式：基于已有剧情续写新章节
     * @param bookUuid 书籍UUID
     * @param prompt 章节提示
     * @return 生成的内容
     */
    public String continueStory(String bookUuid, GenerationPrompt.ChapterPrompt prompt) {
        log.info("开始续写模式，书籍UUID: {}", bookUuid);

        // 1. 获取上下文
        String query = prompt.getRecentChaptersSummary() != null ? prompt.getRecentChaptersSummary() : "主角当前状态";
        GenerationPrompt.GenerationContext context = retrieveContext(bookUuid, query);

        // 2. 设置书籍信息
        context.setBookName(getBookName(bookUuid));
        context.setWritingStyle("传统玄幻");

        // 3. 构建提示
        String systemPrompt = generationPrompt.buildContinueSystemPrompt(context);
        String userPrompt = generationPrompt.buildContinueUserPrompt(context, prompt);

        // 4. 调用LLM生成
        return generateContent(systemPrompt, userPrompt);
    }

    /**
     * 概要模式：用户指定情节概要，AI补充细节
     * @param bookUuid 书籍UUID
     * @param outline 情节概要
     * @param prompt 章节提示
     * @return 生成的内容
     */
    public String generateFromOutline(String bookUuid, String outline, GenerationPrompt.ChapterPrompt prompt) {
        log.info("开始概要模式，书籍UUID: {}, 概要: {}", bookUuid, outline);

        // 1. 获取上下文
        GenerationPrompt.GenerationContext context = retrieveContext(bookUuid, outline);

        // 2. 设置书籍信息
        context.setBookName(getBookName(bookUuid));
        context.setWritingStyle("传统玄幻");

        // 3. 构建提示
        String systemPrompt = generationPrompt.buildOutlineSystemPrompt(context, outline);
        String userPrompt = generationPrompt.buildOutlineUserPrompt(context, prompt, outline);

        // 4. 调用LLM生成
        return generateContent(systemPrompt, userPrompt);
    }

    /**
     * 混合模式：结合续写和概要
     * @param bookUuid 书籍UUID
     * @param content 续写内容
     * @param outline 补充概要
     * @param prompt 章节提示
     * @return 生成的内容
     */
    public String generateHybrid(String bookUuid, String content, String outline, GenerationPrompt.ChapterPrompt prompt) {
        log.info("开始混合模式，书籍UUID: {}", bookUuid);

        // 1. 获取上下文
        GenerationPrompt.GenerationContext context = retrieveContext(bookUuid, content + " " + outline);

        // 2. 设置书籍信息
        context.setBookName(getBookName(bookUuid));
        context.setWritingStyle("传统玄幻");

        // 3. 构建提示
        String systemPrompt = generationPrompt.buildHybridSystemPrompt(context, content, outline);
        String userPrompt = generationPrompt.buildOutlineUserPrompt(context, prompt, outline);

        // 4. 调用LLM生成
        return generateContent(systemPrompt, userPrompt);
    }

    /**
     * 交互式问答
     * @param bookUuid 书籍UUID
     * @param question 用户问题
     * @return 回答内容
     */
    public String chat(String bookUuid, String question) {
        log.info("交互式问答，书籍: {}, 问题: {}", bookUuid, question);

        // 1. 获取上下文
        GenerationPrompt.GenerationContext context = retrieveContext(bookUuid, question);

        // 2. 构建问答提示
        String systemPrompt = String.format("""
                # 问答任务

                你是一个小说知识问答助手。根据以下知识图谱信息回答用户的问题。

                ## 知识图谱信息

                人物状态：
                %s

                历史事件：
                %s

                请根据以上信息回答问题。如果无法从知识图谱中找到答案，请说明"无法从已知信息中找到答案"。
                """,
                formatCharacterStates(context.getCharacterStates()),
                formatHistoricalEvents(context.getHistoricalEvents())
        );

        // 3. 调用LLM
        return generateContent(systemPrompt, question);
    }

    /**
     * 查询人物当前状态
     * @param bookUuid 书籍UUID
     * @param characterName 人物名称
     * @return 人物状态
     */
    public CharacterStateInfo getCharacterState(String bookUuid, String characterName) {
        CharacterStateInfo info = new CharacterStateInfo();
        info.setCharacterName(characterName);

        // 查询当前状态
        List<Map<String, Object>> states = graphRagService.getEntityCurrentState(bookUuid, characterName);
        info.setCurrentStates(states);

        // 查询历史事件
        List<Map<String, Object>> history = graphRagService.getEntityHistoryEvents(bookUuid, characterName);
        info.setRecentEvents(history.stream()
                .sorted((e1, e2) -> Integer.compare(
                        (Integer) e2.get("chapterIndex"),
                        (Integer) e1.get("chapterIndex")))
                .limit(10)
                .collect(Collectors.toList()));

        return info;
    }

    /**
     * 查询书籍基本信息
     */
    private String getBookName(String bookUuid) {
        // 简化处理，实际应该从MongoDB查询
        return "未知书籍";
    }

    /**
     * 查询相关实体
     */
    private List<String> queryRelatedEntities(String bookUuid, String query) {
        // 使用RAG检索相关实体
        return novelEntityService.retrieveEntities(bookUuid, query, 10)
                .stream()
                .map(e -> e.getStandardName())
                .collect(Collectors.toList());
    }

    /**
     * 调用LLM生成内容
     */
    private String generateContent(String systemPrompt, String userPrompt) {
        // 这里需要调用实际的LLM
        // 简化处理，返回提示
        return "生成内容占位符";
    }

    /**
     * 格式化人物状态
     */
    private String formatCharacterStates(List<GenerationPrompt.CharacterState> states) {
        if (states == null || states.isEmpty()) {
            return "无";
        }
        return states.stream()
                .map(s -> String.format("%s: %s", s.getCharacterName(), s.getRealm()))
                .collect(Collectors.joining("\n"));
    }

    /**
     * 格式化历史事件
     */
    private String formatHistoricalEvents(List<GenerationPrompt.HistoricalEvent> events) {
        if (events == null || events.isEmpty()) {
            return "无";
        }
        return events.stream()
                .limit(10)
                .map(e -> String.format("第%d章: %s - %s", e.getChapterIndex(), e.getEventType(), e.getDescription()))
                .collect(Collectors.joining("\n"));
    }

    /**
     * 人物状态信息
     */
    @Data
    public static class CharacterStateInfo {
        private String characterName;
        private List<Map<String, Object>> currentStates;
        private List<Map<String, Object>> recentEvents;
    }
}
