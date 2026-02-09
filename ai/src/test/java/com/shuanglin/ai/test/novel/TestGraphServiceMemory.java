package com.shuanglin.ai.test.novel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shuanglin.bot.utils.FileReadUtil;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.Data;

import java.io.File;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 知识图谱构建测试 - 内存存储版
 * 独立运行，不依赖Spring和数据库
 * 基于 GraphService.readStory() 方法实现
 */
public class TestGraphServiceMemory {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static OpenAiChatModel chatModel;

    // ==================== 内存存储 ====================

    /** 实体存储: entityUuid -> entityData */
    private static final Map<String, EntityData> ENTITY_STORE = new ConcurrentHashMap<>();

    /** 事件存储: eventUuid -> eventData */
    private static final Map<String, EventData> EVENT_STORE = new ConcurrentHashMap<>();

    /** 状态存储: stateUuid -> stateData */
    private static final Map<String, StateData> STATE_STORE = new ConcurrentHashMap<>();

    /** 关系存储 */
    private static final List<RelationData> RELATION_STORE = new ArrayList<>();

    /** 章节处理报告 */
    private static final List<ChapterReport> CHAPTER_REPORTS = new ArrayList<>();

    public static void main(String[] args) {
        try {
            String epubPath = "D:\\project\\ai-studio\\ai\\src\\main\\resources\\21869-从姑获鸟开始【搜笔趣阁www.sobqg.com】.epub";

            System.out.println("=== 知识图谱构建测试（内存版） ===");
            System.out.println("小说路径: " + epubPath);

            // 初始化LLM
            initializeLLM();

            // 执行构建
            TestGraphServiceMemory service = new TestGraphServiceMemory();
            service.buildGraph(epubPath);

            // 输出结果
            service.outputResults();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化LLM模型
     */
    private static void initializeLLM() {
        System.out.println("初始化MiniMax模型...");
        chatModel = OpenAiChatModel.builder()
                .baseUrl("https://api.minimaxi.com/v1")
                .apiKey("eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJHcm91cE5hbWUiOiJTaHVhbmdsaW4iLCJVc2VyTmFtZSI6IlNodWFuZ2xpbiIsIkFjY291bnQiOiIiLCJTdWJqZWN0SUQiOiIxOTg1NjUzMDM4MDkzNzA1NTkwIiwiUGhvbmUiOiIxODc3Nzc5MTY0NSIsIkdyb3VwSUQiOiIxOTg1NjUzMDM4MDg1MzE2OTgyIiwiUGFnZU5hbWUiOiIiLCJNYWlsIjoiIiwiQ3JlYXRlVGltZSI6IjIwMjUtMTEtMDYgMTQ6MzQ6NDUiLCJUb2tlblR5cGUiOjEsImlzcyI6Im1pbmltYXgifQ.CIsWfl6R1lfBH34ya0Q1H0zYFHT4bQ5LhJAnH4Q6JGgnPXZ-Xp_CVITmk7Nspbck5EkOGuaKe5zrqfaXyfK_3MuItTwY8Qj3YTrGJanX1dIZGLELBNdOExClVDTZLPNK5c5YOilvGczo5Uw7EMnJIb_WGBgFbYKBOyL1M4pGLnrcOtwlDZ-kIZ2Ifgee9JqVY5Y4sVpvsJA3G2JiP9Cb5q24GXrWEvZlcxg-QAqOKwbiPuki_hI6dI_6pdKrUQwm6Iu8iC-xZP6Akayn4GZ6XDBCcne4gMkYVMARAIWyhIfZbeLkS7tyMItadqAgE6aCG6fRRa6xXgZ2RXDUEr4Phg")
                .modelName("MiniMax-M2.1")
                .customHeaders(Map.of("reasoning_split", "true"))
                .temperature(0.0)
                .timeout(Duration.ofSeconds(60000))
                .build();
        System.out.println("✓ 模型初始化完成\n");
    }

    /**
     * 构建知识图谱
     */
    public void buildGraph(String epubPath) {
        File storyFile = new File(epubPath);
        List<FileReadUtil.ParseResult> parseResults = FileReadUtil.readEpubFile(storyFile);

        System.out.println("开始处理 " + parseResults.size() + " 个章节...\n");

        // 只处理前10章作为测试
        int totalToProcess = Math.min(parseResults.size(), 10);

        for (int chapterIdx = 2; chapterIdx < totalToProcess; chapterIdx++) {
            FileReadUtil.ParseResult currentChapter = parseResults.get(chapterIdx);

            // 聚合段落为完整章节文本
            String lastChapterText = aggregateParagraphs(getContentList(parseResults, chapterIdx - 1));
            String currentChapterText = aggregateParagraphs(currentChapter.getContentList());
            String nextChapterText = aggregateParagraphs(getContentList(parseResults, chapterIdx + 1));

            String chapterTitle = currentChapter.getTitle();
            int chapterIndex = chapterIdx + 1;

            System.out.println("处理第 " + chapterIndex + "/" + totalToProcess + " 章: " + chapterTitle);

            try {
                // 调用LLM生成Cypher
                String cypher = generateCypher(chapterTitle, chapterIndex, lastChapterText, currentChapterText, nextChapterText);

                if (cypher == null || cypher.isEmpty()) {
                    System.out.println("  ⚠️  LLM返回空结果");
                    CHAPTER_REPORTS.add(new ChapterReport(chapterIndex, chapterTitle, false));
                    continue;
                }

                System.out.println("\nLLM原始输出:");
                System.out.println("-------------------");
                System.out.println(cypher);
                System.out.println("-------------------\n");

                // 清理Cypher
                cypher = cleanCypher(cypher);

                // 验证Cypher
                if (validateCypher(cypher)) {
                    // 执行Cypher（解析并存储到内存）
                    executeCypherMemory(cypher, chapterIndex, chapterTitle);
                    System.out.println("  ✅ 成功处理");
                } else {
                    System.out.println("  ⚠️  验证失败，跳过执行");
                }

                // 记录报告
                CHAPTER_REPORTS.add(new ChapterReport(chapterIndex, chapterTitle, true));

            } catch (Exception e) {
                System.err.println("  ❌ 处理失败: " + e.getMessage());
                e.printStackTrace();
                CHAPTER_REPORTS.add(new ChapterReport(chapterIndex, chapterTitle, false));
            }

            // 短暂延迟避免API限流
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("\n📊 知识图谱构建完成！");
    }

    /**
     * 调用LLM生成Cypher
     */
    private String generateCypher(String chapterTitle, int chapterIndex,
                                   String lastContext, String currentContext, String nextContext) {
        String prompt = buildUserPrompt(chapterTitle, chapterIndex, lastContext, currentContext, nextContext);

        try {
            return chatModel.chat(prompt);
        } catch (Exception e) {
            System.err.println("LLM调用失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 构建用户提示词
     */
    private String buildUserPrompt(String chapterTitle, int chapterIndex,
                                    String lastContext, String currentContext, String nextContext) {
        return String.format("""
            ## 当前任务
            请基于SystemPrompt中定义的强制性约束规则，处理以下输入：

            【章节信息】
            - 章节标题：%s
            - 章节索引：%s

            【文本内容】
            lastContext（上一章完整内容）：
            %s

            indexText（当前章完整内容）：
            %s

            nextContext（下一章完整内容）：
            %s

            ## 输出要求
            1. 识别本章中的事件（状态变化）
            2. 识别或关联实体
            3. 为每个实体创建/更新状态
            4. 生成完整的Cypher语句
            5. 只返回Cypher语句，用```cypher包裹，不要有其他文字

            请严格按照以下格式输出：
            ```cypher
            // 创建实体
            CREATE (e1:Entity {uuid: 'xxx', entityType: 'Character', name: '角色名', firstMentionChapter: %d})

            // 创建事件
            CREATE (ev1:Event {uuid: 'xxx', chapterIndex: %d, eventType: 'Generation', source: '第%d章 %s', confidence: 0.9, description: '事件描述'})

            // 创建关系
            CREATE (ev1)-[:GENERATES {chapterIndex: %d}]->(e1)
            ```
            """, chapterTitle, chapterIndex, lastContext, currentContext, nextContext, chapterIndex, chapterIndex, chapterIndex, chapterTitle, chapterIndex);
    }

    /**
     * 聚合段落列表为完整章节文本
     */
    private String aggregateParagraphs(List<String> contentList) {
        if (contentList == null || contentList.isEmpty()) {
            return "";
        }

        return contentList.stream()
                .filter(paragraph -> paragraph != null && !paragraph.trim().isEmpty())
                .reduce((p1, p2) -> p1 + "\n" + p2)
                .orElse("");
    }

    private List<String> getContentList(List<FileReadUtil.ParseResult> results, int index) {
        if (index < 0 || index >= results.size()) {
            return Collections.emptyList();
        }
        return results.get(index).getContentList();
    }

    /**
     * 清理Cypher语句
     */
    private String cleanCypher(String cypher) {
        if (cypher == null) {
            return null;
        }

        String cleaned = cypher.trim();

        // 移除markdown代码块标记
        if (cleaned.startsWith("```cypher")) {
            cleaned = cleaned.substring(9).trim();
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3).trim();
        }

        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3).trim();
        }

        // 移除思考标签
        cleaned = removeThinkingTags(cleaned);

        return cleaned;
    }

    /**
     * 移除思考标签
     */
    private String removeThinkingTags(String input) {
        if (input == null) {
            return null;
        }
        return input.replaceAll("(?s)<think>.*?</think>", "")
                    .replaceAll("(?s)<thought>.*?</thought>", "");
    }

    /**
     * 验证Cypher语句
     */
    private boolean validateCypher(String cypher) {
        if (cypher == null || cypher.trim().isEmpty()) {
            return false;
        }

        // 验证Event节点不应包含paragraphIndex属性（章节级处理）
        if (cypher.contains("paragraphIndex:")) {
            System.err.println("  ⚠️  验证失败：Event节点不应包含paragraphIndex属性");
            return false;
        }

        // 验证source格式
        if (cypher.contains("source:") && cypher.contains(" - P")) {
            System.err.println("  ⚠️  验证失败：source格式不应包含段落标记");
            return false;
        }

        // 验证是否包含基本的Cypher关键字
        if (!cypher.contains("CREATE") && !cypher.contains("MERGE")) {
            System.err.println("  ⚠️  验证失败：Cypher应包含CREATE或MERGE关键字");
            return false;
        }

        return true;
    }

    /**
     * 执行Cypher到内存存储
     */
    private void executeCypherMemory(String cypher, int chapterIndex, String chapterTitle) {
        // 提取实体
        Pattern entityPattern = Pattern.compile("(CREATE|MERGE)\\s*\\((\\w+):Entity\\s*\\{([^}]+)\\}\\)");
        Matcher entityMatcher = entityPattern.matcher(cypher);

        while (entityMatcher.find()) {
            String varName = entityMatcher.group(2);
            String props = entityMatcher.group(3);

            EntityData entity = parseEntityData(varName, props, chapterIndex);
            ENTITY_STORE.put(entity.getUuid(), entity);
        }

        // 提取事件
        Pattern eventPattern = Pattern.compile("(CREATE|MERGE)\\s*\\((\\w+):Event\\s*\\{([^}]+)\\}\\)");
        Matcher eventMatcher = eventPattern.matcher(cypher);

        while (eventMatcher.find()) {
            String varName = eventMatcher.group(2);
            String props = eventMatcher.group(3);

            EventData event = parseEventData(varName, props, chapterIndex, chapterTitle);
            EVENT_STORE.put(event.getUuid(), event);
        }

        // 提取状态
        Pattern statePattern = Pattern.compile("(CREATE|MERGE)\\s*\\((\\w+):State\\s*\\{([^}]+)\\}\\)");
        Matcher stateMatcher = statePattern.matcher(cypher);

        while (stateMatcher.find()) {
            String varName = stateMatcher.group(2);
            String props = stateMatcher.group(3);

            StateData state = parseStateData(varName, props, chapterIndex);
            STATE_STORE.put(state.getUuid(), state);
        }

        // 提取关系
        Pattern relPattern = Pattern.compile("\\((\\w+)\\)(?:-|:\\w*-)?\\[?[:\\w]*]?-\\[:(?:\\w+)]->\\((\\w+)\\)");
        Matcher relMatcher = relPattern.matcher(cypher);

        while (relMatcher.find()) {
            String fromVar = relMatcher.group(1);
            String toVar = relMatcher.group(2);

            // 尝试获取关系类型
            Pattern relTypePattern = Pattern.compile("\\[(?::-)?:(\\w+)\\]");
            Matcher relTypeMatcher = relTypePattern.matcher(cypher.substring(relMatcher.start(), relMatcher.end()));
            String relType = relTypeMatcher.find() ? relTypeMatcher.group(1) : "RELATED";

            RelationData relation = new RelationData();
            relation.setFromVar(fromVar);
            relation.setToVar(toVar);
            relation.setRelationType(relType);
            relation.setChapterIndex(chapterIndex);
            RELATION_STORE.add(relation);
        }
    }

    /**
     * 解析实体数据
     */
    private EntityData parseEntityData(String varName, String props, int chapterIndex) {
        EntityData entity = new EntityData();
        entity.setUuid(UUID.randomUUID().toString());
        entity.setEntityType("Unknown");
        entity.setCreatedAt(chapterIndex);
        entity.setFirstMentionChapter(chapterIndex);

        // 解析属性
        Pattern propPattern = Pattern.compile("(\\w+):\\s*[\"']([^\"']+)[\"']");
        Matcher propMatcher = propPattern.matcher(props);

        while (propMatcher.find()) {
            String key = propMatcher.group(1);
            String value = propMatcher.group(2);

            switch (key) {
                case "uuid" -> entity.setUuid(value);
                case "entityType" -> entity.setEntityType(value);
                case "name" -> entity.setName(value);
                case "firstMentionChapter" -> entity.setFirstMentionChapter(Integer.parseInt(value));
            }
        }

        if (entity.getName() == null) {
            entity.setName(varName);
        }

        return entity;
    }

    /**
     * 解析事件数据
     */
    private EventData parseEventData(String varName, String props, int chapterIndex, String chapterTitle) {
        EventData event = new EventData();
        event.setUuid(UUID.randomUUID().toString());
        event.setChapterIndex(chapterIndex);
        event.setSource("第" + chapterIndex + "章 " + chapterTitle);
        event.setConfidence(0.9);

        // 解析属性
        Pattern propPattern = Pattern.compile("(\\w+):\\s*[\"']([^\"']+)[\"']|(\\w+):\\s*(\\d+\\.?\\d*)");
        Matcher propMatcher = propPattern.matcher(props);

        while (propMatcher.find()) {
            String key = propMatcher.group(1) != null ? propMatcher.group(1) : propMatcher.group(3);
            String value = propMatcher.group(2) != null ? propMatcher.group(2) : propMatcher.group(4);

            switch (key) {
                case "uuid" -> event.setUuid(value);
                case "eventType" -> event.setEventType(value);
                case "description" -> event.setDescription(value);
                case "confidence" -> event.setConfidence(Double.parseDouble(value));
            }
        }

        if (event.getEventType() == null) {
            event.setEventType("Unknown");
        }

        return event;
    }

    /**
     * 解析状态数据
     */
    private StateData parseStateData(String varName, String props, int chapterIndex) {
        StateData state = new StateData();
        state.setUuid(UUID.randomUUID().toString());
        state.setValidFromChapter(chapterIndex);

        // 解析属性
        Pattern propPattern = Pattern.compile("(\\w+):\\s*[\"']([^\"']+)[\"']|(\\w+):\\s*(\\d+)");
        Matcher propMatcher = propPattern.matcher(props);

        while (propMatcher.find()) {
            String key = propMatcher.group(1) != null ? propMatcher.group(1) : propMatcher.group(3);
            String value = propMatcher.group(2) != null ? propMatcher.group(2) : propMatcher.group(4);

            switch (key) {
                case "uuid" -> state.setUuid(value);
                case "stateType" -> state.setStateType(value);
                case "stateValue" -> state.setStateValue(value);
                case "valid_from_chapter" -> state.setValidFromChapter(Integer.parseInt(value));
                case "valid_to_chapter" -> {
                    if (value != null && !value.equals("null")) {
                        state.setValidToChapter(Integer.parseInt(value));
                    }
                }
            }
        }

        return state;
    }

    /**
     * 输出结果
     */
    public void outputResults() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("【知识图谱构建结果】");
        System.out.println("=".repeat(80));

        // 统计信息
        System.out.println("\n=== 统计 ===");
        System.out.println("实体数量: " + ENTITY_STORE.size());
        System.out.println("事件数量: " + EVENT_STORE.size());
        System.out.println("状态数量: " + STATE_STORE.size());
        System.out.println("关系数量: " + RELATION_STORE.size());
        System.out.println("处理章节: " + CHAPTER_REPORTS.size());

        // 按类型统计实体
        Map<String, Long> entityTypeCount = new HashMap<>();
        for (EntityData entity : ENTITY_STORE.values()) {
            entityTypeCount.merge(entity.getEntityType(), 1L, Long::sum);
        }
        System.out.println("\n实体类型分布:");
        entityTypeCount.forEach((type, count) -> System.out.println("  " + type + ": " + count));

        // 输出实体详情
        System.out.println("\n=== 实体详情 ===");
        for (EntityData entity : ENTITY_STORE.values()) {
            System.out.println("\n[" + entity.getUuid().substring(0, 8) + "] " + entity.getName() + " (" + entity.getEntityType() + ")");
            System.out.println("  首次出现章节: " + entity.getFirstMentionChapter());
        }

        // 输出事件详情
        System.out.println("\n=== 事件详情 ===");
        for (EventData event : EVENT_STORE.values()) {
            System.out.println("\n[" + event.getUuid().substring(0, 8) + "] " + event.getEventType());
            System.out.println("  章节: " + event.getChapterIndex());
            System.out.println("  描述: " + event.getDescription());
        }

        // 输出JSON格式结果
        System.out.println("\n\n=== JSON格式输出 ===");
        try {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("statistics", Map.of(
                    "entityCount", ENTITY_STORE.size(),
                    "eventCount", EVENT_STORE.size(),
                    "stateCount", STATE_STORE.size(),
                    "relationCount", RELATION_STORE.size(),
                    "chapterCount", CHAPTER_REPORTS.size()
            ));

            Map<String, List<Map<String, Object>>> entitiesByType = new LinkedHashMap<>();
            for (EntityData entity : ENTITY_STORE.values()) {
                Map<String, Object> entityMap = new LinkedHashMap<>();
                entityMap.put("uuid", entity.getUuid());
                entityMap.put("name", entity.getName());
                entityMap.put("type", entity.getEntityType());
                entityMap.put("firstMentionChapter", entity.getFirstMentionChapter());

                entitiesByType
                        .computeIfAbsent(entity.getEntityType(), k -> new ArrayList<>())
                        .add(entityMap);
            }
            result.put("entitiesByType", entitiesByType);

            System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
        } catch (Exception e) {
            System.err.println("JSON输出失败: " + e.getMessage());
        }
    }

    // ==================== 内部类 ====================

    @Data
    public static class EntityData {
        private String uuid;
        private String entityType;
        private String name;
        private int createdAt;
        private int firstMentionChapter;
        private String firstMentionSource;
    }

    @Data
    public static class EventData {
        private String uuid;
        private int chapterIndex;
        private String eventType;
        private String source;
        private double confidence;
        private String description;
    }

    @Data
    public static class StateData {
        private String uuid;
        private int validFromChapter;
        private Integer validToChapter;
        private String stateType;
        private String stateValue;
    }

    @Data
    public static class RelationData {
        private String fromVar;
        private String toVar;
        private String relationType;
        private int chapterIndex;
    }

    @Data
    public static class ChapterReport {
        private int chapterIndex;
        private String chapterTitle;
        private boolean success;
        private String cypher;

        public ChapterReport(int chapterIndex, String chapterTitle, boolean success) {
            this.chapterIndex = chapterIndex;
            this.chapterTitle = chapterTitle;
            this.success = success;
        }
    }
}
