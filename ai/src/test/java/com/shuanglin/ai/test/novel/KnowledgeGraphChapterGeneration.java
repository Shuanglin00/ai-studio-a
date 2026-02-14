package com.shuanglin.ai.test.novel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shuanglin.ai.utils.FileReadUtil;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.Data;

import java.io.File;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 知识图谱检索增强章节生成
 *
 * 流程：
 * 1. 分析：接收上一章内容 + 下一章大纲，LLM分析需要参考的实体
 * 2. 检索：从Milvus/内存中检索相关实体设定
 * 3. 构建：将实体设定构建成Prompt上下文
 * 4. 生成：LLM根据增强的Prompt生成下一章节
 */
public class KnowledgeGraphChapterGeneration {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static OpenAiChatModel chatModel;

    // ==================== 内存存储（模拟Milvus） ====================

    /** 实体存储: name -> Entity */
    private static final Map<String, Entity> ENTITY_STORE = new ConcurrentHashMap<>();

    /** 关系存储 */
    private static final Map<String, Set<String>> ENTITY_RELATIONS = new ConcurrentHashMap<>();

    /** 实体类型常量 */
    public static final String TYPE_CHARACTER = "Character";
    public static final String TYPE_LOCATION = "Location";
    public static final String TYPE_IDENTITY = "Identity";
    public static final String TYPE_RULE = "Rule";
    public static final String TYPE_SKILL = "Skill";
    public static final String TYPE_LEGACY = "Legacy";
    public static final String TYPE_CONCEPT = "Concept";

    public static void main(String[] args) {
        try {
            String epubPath = "D:\\project\\ai-studio\\ai\\src\\main\\resources\\21869-从姑获鸟开始【搜笔趣阁www.sobqg.com】.epub";

            System.out.println("=== 知识图谱检索增强章节生成 ===");
            System.out.println("小说路径: " + epubPath);

            // 1. 初始化LLM
            initializeLLM();

            // 2. 加载知识图谱（从小说中提取）
            System.out.println("\n【步骤1】从小说中提取实体，构建知识图谱...");
            loadKnowledgeGraph(epubPath);
            System.out.println("知识图谱加载完成，共 " + ENTITY_STORE.size() + " 个实体");

            // 3. 模拟上一章内容和下一章大纲
            String previousChapter = """
                第三章：觉醒

                李阎站在城墙之上，望着远方的山脉。
                十八翅九头怪物出现在他面前："唤醒她，我的行走。"
                李阎觉醒了阎浮行走的身份。
                太岁在一旁说道："从今天起，你就是阎浮行走了。"
                """;

            String nextChapterOutline = """
                1. 李阎与其他阎浮行走相遇
                2. 行走之间发生理念冲突
                3. 李阎展示阎浮行走的能力
                4. 揭示阎浮行走的新规则
                """;

            // 4. 执行章节生成
            System.out.println("\n【步骤2】分析并检索相关实体...");
            String previousChapterEnhanced = enhancePreviousChapter(previousChapter);

            System.out.println("\n【步骤3】构建增强Prompt...");
            String enhancedPrompt = buildEnhancedPrompt(previousChapterEnhanced, nextChapterOutline);

            System.out.println("\n【步骤4】调用LLM生成下一章节...");
            // 注意：这里实际调用会生成内容，为演示只输出Prompt结构
            System.out.println("\n生成的Prompt预览：");
            System.out.println("=".repeat(80));
            System.out.println(enhancedPrompt.substring(0, Math.min(2000, enhancedPrompt.length())));
            System.out.println("=".repeat(80));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ==================== 步骤1：初始化 ====================

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

    // ==================== 步骤2：加载知识图谱 ====================

    /**
     * 从小说中提取实体，构建知识图谱
     */
    private static void loadKnowledgeGraph(String epubPath) {
        File storyFile = new File(epubPath);
        List<FileReadUtil.ParseResult> parseResults = FileReadUtil.readEpubFile(storyFile);

        // 只处理前20章作为测试
        int totalToProcess = Math.min(parseResults.size(), 20);

        for (int i = 2; i < totalToProcess; i++) {
            String chapterText = parseResults.get(i).getContent();
            System.out.println("处理第 " + (i + 1) + " 章...");

            try {
                // 调用LLM提取实体
                List<Entity> entities = extractEntitiesFromChapter(chapterText, i + 1);

                // 存储到内存
                for (Entity entity : entities) {
                    ENTITY_STORE.put(entity.getName(), entity);
                    // 建立别名索引
                    for (String alias : entity.getAliases()) {
                        ENTITY_STORE.put(alias, entity);
                    }
                }

            } catch (Exception e) {
                System.err.println("  处理第 " + (i + 1) + " 章失败: " + e.getMessage());
            }

            // 避免API限流
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 从章节中提取实体
     * 参考 TestNovelGraphBuild 的实体提取 Prompt
     */
    private static List<Entity> extractEntitiesFromChapter(String chapterText, int chapterNum) {
        String prompt = String.format("""
                                              # 任务：全局实体提取

                                              请从[文本内容]中提取所有静态实体，包括：
                                              - Character: 角色（人名、角色名）
                                              - Location: 地点（地名、场景）
                                              - Organization: 组织（宗门、家族、势力）
                                              - Item: 物品（武器、法宝、道具）
                                              - Skill: 技能/功法（法术、神通、功法）
                                              - Identity: 身份/称号（身份、称号、头衔）
                                              - Rule: 规则/法则（世界规则、制度、门规）
                                              - Era: 时代/纪元（时代、纪元、历史时期）
                                              - Species: 种族/物种（种族、妖兽、灵族）
                                              - Realm: 境界/等级（修炼境界、等级划分）
                                              - Constitution: 体质/血脉（特殊体质、血脉）
                                              - Currency: 货币/资源（灵石、灵晶、功德）
                                              - Concept: 抽象概念（气运、业力、因果）
                                              - Legacy: 传承（传承、秘法）
                                              - Phenomenon: 天地异象（天劫、异象）

                                              ## 提取规则
                                              1. **专有名词优先**：仅提取具有明确指代的专有名词（如"萧炎"、"云岚宗"、"玄重尺"）
                                              2. **别名聚合**：识别同一实体的所有别名（如"萧炎"的别名："岩枭"、"萧家三少爷"、"炎帝"）
                                              3. **过滤泛指词**：忽略泛指词（如"黑衣人"、"老者"、"那个人"）
                                              4. **首次出现记录**：记录实体首次出现的完整句子或段落作为上下文
                                              5. **提取属性与设定**：从原文中提取该实体的所有属性、特征、背景设定
                                              6. **跳过无效内容**：如果文本中没有可提取的实体（如目录、版权声明等），返回空数组 []

                                              ## 输出格式

                                              请**只返回JSON数组**，不要包含任何其他文字说明。

                                              如果没有可提取的实体，返回：
                                              ```

                                              []
                                              ```

                                              如果有实体，每个元素包含以下字段：
                                              ```
                                              [
                                                {
                                                  "standardName": "实体标准名称",
                                                  "aliases": ["别名1", "别名2"],
                                                  "entityType": "Character/Location/Organization/Item/Skill/Identity/Rule/Era/Species/Realm/Constitution/Currency/Concept/Legacy/Phenomenon",
                                                  "firstMention": "首次出现的完整句子或段落（保持原文）",
                                                  "contexts": [
                                                    "包含该实体的句子1（原文）",
                                                    "包含该实体的句子2（原文）",
                                                    "包含该实体的句子3（原文）"
                                                  ],
                                                  "attributes": {
                                                    "description": "实体的外貌/特征描述（从原文提取）",
                                                    "background": "背景信息（从原文提取）",
                                                    "abilities": "能力/技能（从原文提取）",
                                                    "relationships": "与其他实体的关系（从原文提取）",
                                                    "other": "其他重要信息（从原文提取）"
                                                  }
                                                }
                                              ]
                                              ```

                                              ## 重要说明
                                              - **必须只返回JSON数组，不要有任何解释性文字**
                                              - firstMention、contexts、attributes中的所有内容必须直接从原文中提取，不要概括或改写
                                              - contexts数组中至少包含3-5个包含该实体的原文句子
                                              - attributes中的每个字段都应该引用原文中的具体描述

                                              ## [文本内容]

                                              %s
                                              """, chapterText);

        try {
            String response = chatModel.chat(prompt);
            response = cleanThinkingTags(response);
            response = extractJsonArray(response);

            List<Map<String, Object>> jsonList = objectMapper.readValue(response,
                new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {});

            List<Entity> entities = new ArrayList<>();
            for (Map<String, Object> json : jsonList) {
                Entity entity = new Entity();
                entity.setName((String) json.get("standardName"));  // 与 TestNovelGraphBuild 一致
                entity.setType((String) json.get("entityType"));    // 与 TestNovelGraphBuild 一致
                entity.setChapter(chapterNum);

                Object aliasesObj = json.get("aliases");
                if (aliasesObj instanceof List) {
                    entity.setAliases(new ArrayList<>((List<String>) aliasesObj));
                }

                Object descObj = json.get("attributes");
                if (descObj instanceof Map) {
                    Map<String, Object> attrs = (Map<String, Object>) descObj;
                    StringBuilder descBuilder = new StringBuilder();
                    if (attrs.get("description") != null) {
                        descBuilder.append(attrs.get("description"));
                    }
                    if (attrs.get("background") != null) {
                        if (descBuilder.length() > 0) descBuilder.append("; ");
                        descBuilder.append("背景: ").append(attrs.get("background"));
                    }
                    if (attrs.get("abilities") != null) {
                        if (descBuilder.length() > 0) descBuilder.append("; ");
                        descBuilder.append("能力: ").append(attrs.get("abilities"));
                    }
                    entity.setDescription(descBuilder.toString());
                }

                entities.add(entity);
            }
            return entities;

        } catch (Exception e) {
            System.err.println("  解析实体失败: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    // ==================== 步骤3：分析并检索实体 ====================

    /**
     * 分析上一章内容，提取实体用于检索知识图谱
     * 参考 TestNovelGraphBuild 的实体提取 Prompt
     */
    private static String enhancePreviousChapter(String previousChapter) {
        String prompt = String.format("""
                                              # 任务：提取实体用于知识图谱检索

                                              请从[章节内容]中提取所有提到的实体，包括：
                                              - Character: 角色（人名、角色名）
                                              - Location: 地点（地名、场景）
                                              - Organization: 组织（宗门、家族、势力）
                                              - Item: 物品（武器、法宝、道具）
                                              - Skill: 技能/功法（法术、神通、功法）
                                              - Identity: 身份/称号（身份、称号、头衔）
                                              - Rule: 规则/法则（世界规则、制度、门规）
                                              - Era: 时代/纪元（时代、纪元、历史时期）
                                              - Species: 种族/物种（种族、妖兽、灵族）
                                              - Realm: 境界/等级（修炼境界、等级划分）
                                              - Constitution: 体质/血脉（特殊体质、血脉）
                                              - Currency: 货币/资源（灵石、灵晶、功德）
                                              - Concept: 抽象概念（气运、业力、因果）
                                              - Legacy: 传承（传承、秘法）
                                              - Phenomenon: 天地异象（天劫、异象）

                                              ## 提取规则
                                              1. **专有名词优先**：仅提取具有明确指代的专有名词
                                              2. **别名聚合**：识别同一实体的所有别名
                                              3. **过滤泛指词**：忽略泛指词（如"黑衣人"、"老者"、"那个人"）
                                              4. **上下文关联**：记录实体出现的上下文句子
                                              5. **全面提取**：不要遗漏任何可能影响后续剧情的实体

                                              ## 输出格式

                                              请**只返回JSON数组**，不要包含任何其他文字说明。

                                              如果没有可提取的实体，返回：
                                              ```

                                              []
                                              ```

                                              如果有实体，每个元素包含以下字段：
                                              ```
                                              [
                                                {
                                                  "standardName": "实体标准名称",
                                                  "aliases": ["别名1", "别名2"],
                                                  "entityType": "Character/Location/Organization/Item/Skill/Identity/Rule/Era/Species/Realm/Constitution/Currency/Concept/Legacy/Phenomenon",
                                                  "contexts": [
                                                    "包含该实体的句子1（原文）",
                                                    "包含该实体的句子2（原文）",
                                                    "包含该实体的句子3（原文）"
                                                  ]
                                                }
                                              ]
                                              ```

                                              ## 重要说明
                                              - **必须只返回JSON数组，不要有任何解释性文字**
                                              - contexts中的内容必须直接从原文中提取，不要概括或改写
                                              - contexts数组中至少包含3-5个包含该实体的原文句子
                                              - 重点提取对后续剧情有重要影响的实体

                                              ## [章节内容]

                                              %s
                                              """, previousChapter);

        try {
            String response = chatModel.chat(prompt);
            response = cleanThinkingTags(response);
            response = extractJsonArray(response);

            System.out.println("提取的实体JSON:");
            System.out.println(response);
            System.out.println();

            // 解析JSON获取实体名称列表
            List<Map<String, Object>> jsonList = objectMapper.readValue(response,
                new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {});

            List<String> entityNames = new ArrayList<>();
            for (Map<String, Object> json : jsonList) {
                String name = (String) json.get("standardName");
                if (name != null && !name.isEmpty()) {
                    entityNames.add(name);
                }
                // 同时添加别名
                Object aliasesObj = json.get("aliases");
                if (aliasesObj instanceof List) {
                    for (String alias : (List<String>) aliasesObj) {
                        if (alias != null && !alias.isEmpty()) {
                            entityNames.add(alias);
                        }
                    }
                }
            }

            String result = String.join(",", entityNames);
            System.out.println("识别到的实体: " + result);
            return result;

        } catch (Exception e) {
            System.err.println("分析失败: " + e.getMessage());
            return "";
        }
    }

    /**
     * 根据实体名称列表检索设定
     */
    private static List<Entity> retrieveEntities(String entityNames) {
        if (entityNames == null || entityNames.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> names = Arrays.asList(entityNames.split(","));
        List<Entity> results = new ArrayList<>();
        Set<String> added = new HashSet<>();

        for (String name : names) {
            String trimmedName = name.trim();
            if (trimmedName.isEmpty()) continue;

            // 从知识图谱中检索
            Entity entity = ENTITY_STORE.get(trimmedName);
            if (entity != null && !added.contains(entity.getName())) {
                results.add(entity);
                added.add(entity.getName());
                System.out.println("  检索到实体: " + entity.getName() + " (" + entity.getType() + ")");
            }
        }

        return results;
    }

    // ==================== 步骤4：构建增强Prompt ====================

    /**
     * 构建增强的Prompt
     */
    private static String buildEnhancedPrompt(String previousChapter, String nextChapterOutline) {
        StringBuilder prompt = new StringBuilder();

        // 1. 系统指令
        prompt.append("你是一个专业的小说作家，请根据以下信息续写小说。\n\n");

        // 2. 上一章内容
        prompt.append("【上一章内容】\n");
        prompt.append(previousChapter).append("\n\n");

        // 3. 下一章大纲
        prompt.append("【下一章大纲】\n");
        prompt.append(nextChapterOutline).append("\n\n");

        // 4. 检索知识图谱，获取相关实体设定
        List<Entity> entities = retrieveEntities(previousChapter);

        if (!entities.isEmpty()) {
            // 按类型分组
            Map<String, List<Entity>> entitiesByType = new LinkedHashMap<>();
            for (Entity entity : entities) {
                entitiesByType
                        .computeIfAbsent(entity.getType(), k -> new ArrayList<>())
                        .add(entity);
            }

            // 角色设定
            if (entitiesByType.containsKey(TYPE_CHARACTER)) {
                prompt.append("【角色设定】\n");
                for (Entity entity : entitiesByType.get(TYPE_CHARACTER)) {
                    prompt.append(String.format("- %s: %s\n",
                        entity.getName(),
                        entity.getDescription()));
                }
                prompt.append("\n");
            }

            // 身份与规则
            if (entitiesByType.containsKey(TYPE_IDENTITY)) {
                prompt.append("【身份设定】\n");
                for (Entity entity : entitiesByType.get(TYPE_IDENTITY)) {
                    prompt.append(String.format("- %s: %s\n",
                        entity.getName(),
                        entity.getDescription()));
                }
                prompt.append("\n");
            }

            if (entitiesByType.containsKey(TYPE_RULE)) {
                prompt.append("【规则设定】\n");
                for (Entity entity : entitiesByType.get(TYPE_RULE)) {
                    prompt.append(String.format("- %s: %s\n",
                        entity.getName(),
                        entity.getDescription()));
                }
                prompt.append("\n");
            }

            // 技能与传承
            if (entitiesByType.containsKey(TYPE_SKILL) || entitiesByType.containsKey(TYPE_LEGACY)) {
                prompt.append("【能力设定】\n");
                for (String type : Arrays.asList(TYPE_SKILL, TYPE_LEGACY)) {
                    if (entitiesByType.containsKey(type)) {
                        for (Entity entity : entitiesByType.get(type)) {
                            prompt.append(String.format("- %s: %s\n",
                                entity.getName(),
                                entity.getDescription()));
                        }
                    }
                }
                prompt.append("\n");
            }

            // 地点
            if (entitiesByType.containsKey(TYPE_LOCATION)) {
                prompt.append("【地点设定】\n");
                for (Entity entity : entitiesByType.get(TYPE_LOCATION)) {
                    prompt.append(String.format("- %s: %s\n",
                        entity.getName(),
                        entity.getDescription()));
                }
                prompt.append("\n");
            }
        }

        // 5. 写作要求
        prompt.append("【写作要求】\n");
        prompt.append("1. 保持角色性格与上述设定一致\n");
        prompt.append("2. 遵守已设定的规则和世界观\n");
        prompt.append("3. 延续上一章的剧情自然过渡\n");
        prompt.append("4. 根据大纲展开具体情节\n");
        prompt.append("5. 注重场景描写和人物互动\n");
        prompt.append("6. 人物刻画：次要人物也需要有描写，可以是一段话、一个行为或一个细节，\n");
        prompt.append("   通过细微之处反应次要人物的特点（如性格、动机、能力）\n");
        prompt.append("7. 故事为主，细节为辅：用故事推进剧情，用细节丰富人物\n\n");

        prompt.append("请续写下一章内容：");

        return prompt.toString();
    }

    // ==================== 工具方法 ====================

    private static String cleanThinkingTags(String input) {
        if (input == null) return null;
        return input.replaceAll("(?s)<think>.*?</think>", "")
                    .replaceAll("(?s)<thought>.*?</thought>", "");
    }

    private static String extractJsonArray(String response) {
        if (response.contains("```json")) {
            response = response.substring(response.indexOf("```json") + 7);
            response = response.substring(0, response.indexOf("```"));
        } else if (response.contains("```")) {
            response = response.substring(response.indexOf("```") + 3);
            response = response.substring(0, response.indexOf("```"));
        }
        return response.trim();
    }

    // ==================== 内部类 ====================

    @Data
    public static class Entity {
        private String name;
        private String type;
        private String description;
        private List<String> aliases = new ArrayList<>();
        private int chapter;
        private Map<String, Object> attributes = new HashMap<>();
    }

    @Data
    public static class RetrievalResult {
        private List<Entity> characters = new ArrayList<>();
        private List<Entity> locations = new ArrayList<>();
        private List<Entity> identities = new ArrayList<>();
        private List<Entity> rules = new ArrayList<>();
        private List<Entity> skills = new ArrayList<>();
        private List<Entity> legacies = new ArrayList<>();
        private List<Entity> concepts = new ArrayList<>();
    }
}
