import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shuanglin.bot.utils.FileReadUtil;
import com.shuanglin.dao.model.EntityMention;
import com.shuanglin.dao.model.EntityRegistry;
import dev.langchain4j.model.openai.OpenAiChatModel;

import java.io.File;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 小说图谱构建测试 独立运行，不依赖Spring Boot和数据库 LLM结果直接输出到控制台
 */
public class TestNovelGraphBuild {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static OpenAiChatModel chatModel;

    public static void main(String[] args) {
        try {
            // 初始化LLM
            initializeLLM();

            // 读取测试小说文本
            String testNovelPath = "D:\\project\\ai-studio\\ai\\src\\main\\resources\\21869-从姑获鸟开始【搜笔趣阁www.sobqg.com】.epub";
            String bookUuid = "test-book-" + UUID.randomUUID().toString().substring(0, 8);
            String bookName = "测试小说";

            System.out.println("=== 开始小说图谱构建测试 ===");
            System.out.println("书籍UUID: " + bookUuid);
            System.out.println("书籍名称: " + bookName);
            System.out.println();

            TestNovelGraphBuild test = new TestNovelGraphBuild();
            // 执行阶段一：全局扫描与实体标准化
            EntityData entityData = test.scanAndBuildRegistry(testNovelPath);
            List<EntityRegistry> entities = new ArrayList<>(entityData.entities.values());

            // 输出结果
            System.out.println("\n=== 构建完成 ===");
            System.out.println("实体总数: " + entities.size());

            // 按类型统计
            Map<String, Long> typeCount = new HashMap<>();
            for (EntityRegistry entity : entities) {
                typeCount.merge(entity.getEntityType(), 1L, Long::sum);
            }
            System.out.println("\n=== 实体类型统计 ===");
            typeCount.forEach((type, count) ->
                                      System.out.println(type + ": " + count + "个"));

            // 显示所有实体
            System.out.println("\n=== 所有实体详情 ===");
            for (EntityRegistry entity : entities) {
                System.out.println("\n" + entity.getEntityId() + ": " +
                                           entity.getStandardName() + " (" + entity.getEntityType() + ")");
                if (entity.getAliases() != null && !entity.getAliases().isEmpty()) {
                    System.out.println("  别名: " + String.join(", ", entity.getAliases()));
                }
                if (entity.getConfidence() != null) {
                    System.out.println("  置信度: " + String.format("%.2f", entity.getConfidence()));
                }
            }

            // 执行阶段二：设定验证
            System.out.println("\n\n开始阶段二：设定验证...");
            verifyEntitySettings(testNovelPath, entityData);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化LLM模型
     */
    private static void initializeLLM() {
        System.out.println("初始化Ollama模型...");
        chatModel = OpenAiChatModel.builder()
                .baseUrl("https://api.minimaxi.com/v1")
                .apiKey("eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJHcm91cE5hbWUiOiJTaHVhbmdsaW4iLCJVc2VyTmFtZSI6IlNodWFuZ2xpbiIsIkFjY291bnQiOiIiLCJTdWJqZWN0SUQiOiIxOTg1NjUzMDM4MDkzNzA1NTkwIiwiUGhvbmUiOiIxODc3Nzc5MTY0NSIsIkdyb3VwSUQiOiIxOTg1NjUzMDM4MDg1MzE2OTgyIiwiUGFnZU5hbWUiOiIiLCJNYWlsIjoiIiwiQ3JlYXRlVGltZSI6IjIwMjUtMTEtMDYgMTQ6MzQ6NDUiLCJUb2tlblR5cGUiOjEsImlzcyI6Im1pbmltYXgifQ.CIsWfl6R1lfBH34ya0Q1H0zYFHT4bQ5LhJAnH4Q6JGgnPXZ-Xp_CVITmk7Nspbck5EkOGuaKe5zrqfaXyfK_3MuItTwY8Qj3YTrGJanX1dIZGLELBNdOExClVDTZLPNK5c5YOilvGczo5Uw7EMnJIb_WGBgFbYKBOyL1M4pGLnrcOtwlDZ-kIZ2Ifgee9JqVY5Y4sVpvsJA3G2JiP9Cb5q24GXrWEvZlcxg-QAqOKwbiPuki_hI6dI_6pdKrUQwm6Iu8iC-xZP6Akayn4GZ6XDBCcne4gMkYVMARAIWyhIfZbeLkS7tyMItadqAgE6aCG6fRRa6xXgZ2RXDUEr4Phg")
                .modelName("MiniMax-M2.1")
                .customHeaders(Map.of("reasoning_split","true"))
                .temperature(0.0)
                .timeout(Duration.ofSeconds(6000000))
                .build();
        System.out.println("✓ miniMax模型初始化完成\n");
    }
    /**
     * 阶段一：全局扫描与实体标准化
     */
    private EntityData scanAndBuildRegistry(String filePath) {
        System.out.println("【阶段一】开始全局扫描...\n");


        List<FileReadUtil.ParseResult> parseResults = FileReadUtil.readEpubFile(new File(filePath));
        // 2. 并行调用LLM提取实体
        List<EntityMention> allMentions = new ArrayList<>();
        Map<String, List<String>> mentionContextsMap = new HashMap<>();
        Map<String, Map<String, Object>> mentionAttributesMap = new HashMap<>();
        long totalPage = 20;
        for (int i = 2; i < totalPage; i++) {
            String chunk = parseResults.get(i).getContent();
            System.out.println("处理第" + i + "/" + totalPage + "章文本...");

            try {
                String llmResult = extractGlobalEntities(chunk);
                
                System.out.println("\nLLM原始输出:");
                System.out.println("-------------------");
                System.out.println(llmResult);
                System.out.println("-------------------\n");
                
                // 提取JSON部分（去除可能的markdown标记和其他非 JSON内容）
                String jsonContent = llmResult;
                if (jsonContent.contains("```json")) {
                    jsonContent = jsonContent.substring(jsonContent.indexOf("```json") + 7);
                    jsonContent = jsonContent.substring(0, jsonContent.indexOf("```"));
                } else if (jsonContent.contains("```")) {
                    jsonContent = jsonContent.substring(jsonContent.indexOf("```") + 3);
                    jsonContent = jsonContent.substring(0, jsonContent.indexOf("```"));
                }
                jsonContent = jsonContent.trim();
                
                // 如果不是以[开头，尝试找到JSON数组的开始位置
                if (!jsonContent.startsWith("[")) {
                    int arrayStart = jsonContent.indexOf("[");
                    if (arrayStart != -1) {
                        jsonContent = jsonContent.substring(arrayStart);
                    }
                }
                
                // 如果不是以]结尾，尝试找到JSON数组的结束位置
                if (!jsonContent.endsWith("]")) {
                    int arrayEnd = jsonContent.lastIndexOf("]");
                    if (arrayEnd != -1) {
                        jsonContent = jsonContent.substring(0, arrayEnd + 1);
                    }
                }
                
                // 如果提取后仍然不是有效的JSON数组，跳过这块文本
                if (!jsonContent.startsWith("[") || !jsonContent.endsWith("]")) {
                    System.err.println("  警告：无法提取有效的JSON数组，跳过此文本块");
                    System.err.println("  内容预览: " + (jsonContent.length() > 100 ? jsonContent.substring(0, 100) + "..." : jsonContent));
                    continue;
                }
                
                System.out.println("提取的JSON内容:");
                System.out.println(jsonContent);
                System.out.println();
                
                // 修复常见的JSON格式问题
                jsonContent = fixJsonFormat(jsonContent);
                
                System.out.println("修复后的JSON内容:");
                System.out.println(jsonContent);
                System.out.println();

                // 健壮的JSON解析，添加异常处理
                List<Map<String, Object>> jsonList = parseJsonRobustly(jsonContent);

                if (jsonList == null || jsonList.isEmpty()) {
                    System.err.println("  警告: 无法解析JSON，跳过该文本块");
                    continue;
                }

                for (Map<String, Object> json : jsonList) {
                    EntityMention mention = EntityMention.fromJson(json);
                    allMentions.add(mention);
                    
                    // 同时保存contexts和attributes到mention对象
                    // 注意：需要扩展EntityMention类以支持这些字段
                    List<String> contexts = (List<String>) json.get("contexts");
                    Map<String, Object> attributes = (Map<String, Object>) json.get("attributes");
                    
                    // 临时存储：使用Map关联standardName到contexts和attributes
                    if (contexts != null || attributes != null) {
                        String key = mention.getStandardName();
                        if (!mentionContextsMap.containsKey(key)) {
                            mentionContextsMap.put(key, new ArrayList<>());
                            mentionAttributesMap.put(key, new HashMap<>());
                        }
                        if (contexts != null) {
                            mentionContextsMap.get(key).addAll(contexts);
                        }
                        if (attributes != null) {
                            mentionAttributesMap.get(key).putAll(attributes);
                        }
                    }
                    
                    System.out.println("\n  实体: " + mention.getStandardName());
                    if (contexts != null && !contexts.isEmpty()) {
                        System.out.println("    原文上下文数量: " + contexts.size());
                        System.out.println("    示例: " + (contexts.size() > 0 ? contexts.get(0) : ""));
                    }
                    if (attributes != null && !attributes.isEmpty()) {
                        System.out.println("    提取的属性: " + attributes.keySet());
                    }
                }
                System.out.println("  提取到" + jsonList.size() + "个实体提及\n");
            } catch (Exception e) {
                System.err.println("  解析LLM返回结果失败: " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println("\n共提取" + allMentions.size() + "个实体提及");

        // 3. 别名聚合与实体合并
        System.out.println("\n执行别名聚合...");
        EntityData entityData = mergeEntitiesByAlias(allMentions, mentionContextsMap, mentionAttributesMap);
        System.out.println("合并后共" + entityData.entities.size() + "个唯一实体");
            
        // 4. 使用LLM生成实体设定
        System.out.println("\n开始生成实体设定...");
        enrichEntityProfiles(entityData);

        return entityData;
    }

    /**
     * 调用LLM提取全局实体
     */
    private static String extractGlobalEntities(String textChunk) {
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
                                              ```json
                                              []
                                              ```
                                              
                                              如果有实体，每个元素包含以下字段：
                                              ```json
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
                                              """, textChunk);

        String response = chatModel.chat(prompt);

        // 去除thinking标签
        response = removeThinkingTags(response);
        
        // 提取JSON部分（去除可能的markdown标记）
        if (response.contains("```json")) {
            response = response.substring(response.indexOf("```json") + 7);
            response = response.substring(0, response.indexOf("```"));
        } else if (response.contains("```")) {
            response = response.substring(response.indexOf("```") + 3);
            response = response.substring(0, response.indexOf("```"));
        }

        return response.trim();
    }

    /**
     * 别名聚合逻辑 - 返回EntityData包含实体和上下文数据
     */
    private static EntityData mergeEntitiesByAlias(
            List<EntityMention> mentions,
            Map<String, List<String>> mentionContextsMap,
            Map<String, Map<String, Object>> mentionAttributesMap) {
        Map<String, EntityRegistry> result = new HashMap<>();
        Map<String, String> aliasToEntityId = new HashMap<>();
        Map<String, List<String>> entityContexts = new HashMap<>();
        Map<String, Map<String, Object>> entityAttributes = new HashMap<>();
        AtomicInteger idCounter = new AtomicInteger(1);

        for (EntityMention mention : mentions) {
            String entityId = null;

            // 检查是否已有相同名称或别名
            for (String name : mention.getAllNames()) {
                if (aliasToEntityId.containsKey(name)) {
                    entityId = aliasToEntityId.get(name);
                    break;
                }
            }

            // 新实体
            if (entityId == null) {
                entityId = generateEntityId(mention.getType(), idCounter.getAndIncrement());
                EntityRegistry entity = new EntityRegistry();
                entity.setEntityId(entityId);
                entity.setStandardName(mention.getStandardName());
                entity.setEntityType(mention.getType());
                entity.setAliases(mention.getAliases() != null ? mention.getAliases() : new ArrayList<>());
                entity.setConfidence(mention.getConfidence());
                entity.setFirstMentionChapter(1);
                result.put(entityId, entity);
                
                // 初始化上下文和属性集合
                entityContexts.put(entityId, new ArrayList<>());
                entityAttributes.put(entityId, new HashMap<>());
                
                // 收集该实体的contexts和attributes
                String standardName = mention.getStandardName();
                if (mentionContextsMap.containsKey(standardName)) {
                    entityContexts.get(entityId).addAll(mentionContextsMap.get(standardName));
                }
                if (mentionAttributesMap.containsKey(standardName)) {
                    entityAttributes.get(entityId).putAll(mentionAttributesMap.get(standardName));
                }
            } else {
                // 合并别名
                EntityRegistry existing = result.get(entityId);
                if (mention.getAliases() != null) {
                    for (String alias : mention.getAliases()) {
                        existing.addAlias(alias);
                    }
                }
                
                // 合并contexts和attributes
                String standardName = mention.getStandardName();
                if (mentionContextsMap.containsKey(standardName)) {
                    List<String> existingContexts = entityContexts.get(entityId);
                    if (existingContexts != null) {
                        existingContexts.addAll(mentionContextsMap.get(standardName));
                    }
                }
                if (mentionAttributesMap.containsKey(standardName)) {
                    Map<String, Object> existingAttrs = entityAttributes.get(entityId);
                    if (existingAttrs != null) {
                        existingAttrs.putAll(mentionAttributesMap.get(standardName));
                    }
                }
            }

            // 更新别名映射
            for (String name : mention.getAllNames()) {
                aliasToEntityId.put(name, entityId);
            }
        }
        
        // 输出实体的原文上下文和属性设定
        System.out.println("\n=== 实体原文上下文与属性设定 ===");
        for (EntityRegistry entity : result.values()) {
            System.out.println("\n" + entity.getEntityId() + ": " + entity.getStandardName() + " (" + entity.getEntityType() + ")");
            
            List<String> contexts = entityContexts.get(entity.getEntityId());
            if (contexts != null && !contexts.isEmpty()) {
                System.out.println("  原文上下文:");
                for (int i = 0; i < Math.min(contexts.size(), 3); i++) {
                    System.out.println("    - " + contexts.get(i));
                }
            }
            
            Map<String, Object> attrs = entityAttributes.get(entity.getEntityId());
            if (attrs != null && !attrs.isEmpty()) {
                System.out.println("  属性设定:");
                attrs.forEach((key, value) -> {
                    if (value != null && !value.toString().isEmpty()) {
                        System.out.println("    " + key + ": " + value);
                    }
                });
            }
        }

        return new EntityData(result, entityContexts, entityAttributes);
    }
    
    /**
     * 实体数据包装类
     */
    private static class EntityData {
        Map<String, EntityRegistry> entities;
        Map<String, List<String>> entityContexts;
        Map<String, Map<String, Object>> entityAttributes;
        
        EntityData(Map<String, EntityRegistry> entities,
                   Map<String, List<String>> entityContexts,
                   Map<String, Map<String, Object>> entityAttributes) {
            this.entities = entities;
            this.entityContexts = entityContexts;
            this.entityAttributes = entityAttributes;
        }
    }
    
    /**
     * 使用LLM生成实体设定 - 基于原文上下文总结
     */
    private static void enrichEntityProfiles(EntityData entityData) {
        int count = 0;
        int total = entityData.entities.size();
        
        for (EntityRegistry entity : entityData.entities.values()) {
            count++;
            String entityId = entity.getEntityId();
            System.out.println("\n处理 [" + count + "/" + total + "]: " + entity.getStandardName() + " (" + entity.getEntityType() + ")");
            
            // 获取该实体的上下文和属性
            List<String> contexts = entityData.entityContexts.get(entityId);
            Map<String, Object> attributes = entityData.entityAttributes.get(entityId);
            
            // 构造Prompt
            String prompt = buildEntityProfilePrompt(entity, contexts, attributes);
            
            try {
                // 调用LLM生成设定
                String response = chatModel.chat(prompt);
                response = removeThinkingTags(response);
                
                System.out.println("\n生成的设定:");
                System.out.println(response);
                System.out.println("\n" + "=".repeat(80));
                
            } catch (Exception e) {
                System.err.println("生成设定失败: " + e.getMessage());
            }
        }
    }

    /**
     * 阶段二：设定验证 - 带着所有设定重新遍历原文验证是否符合原著
     */
    private static void verifyEntitySettings(String filePath, EntityData entityData) {
        System.out.println("\n\n【阶段二】开始设定验证...\n");
        System.out.println("带着已生成的 " + entityData.entities.size() + " 个实体设定，重新审视原文...\n");

        try {
            // 读取原文
            List<FileReadUtil.ParseResult> parseResults = FileReadUtil.readEpubFile(new File(filePath));

            // 按章节遍历
            int chapterNum = 0;
            for (FileReadUtil.ParseResult result : parseResults) {
                chapterNum++;
                String chapterText = result.getContent();

                if (chapterText == null || chapterText.isEmpty()) {
                    continue;
                }

                // 验证该章节中的实体设定
                System.out.println("验证第 " + chapterNum + " 章: " + result.getTitle());

                // 构建验证Prompt
                String verifyPrompt = buildVerificationPrompt(chapterText, entityData);

                try {
                    String response = chatModel.chat(verifyPrompt);
                    response = removeThinkingTags(response);

                    // 输出验证结果摘要
                    System.out.println("\n验证结果:");
                    System.out.println(response);
                    System.out.println("\n" + "-".repeat(80));

                    // 短暂休息，避免API限流
                    Thread.sleep(1000);

                    if (chapterNum == 20 ){
                        break;
                    }
                } catch (Exception e) {
                    System.err.println("  验证第 " + chapterNum + " 章失败: " + e.getMessage());
                }
            }

            System.out.println("\n\n【验证完成】所有实体设定已与原文对照验证");

            // 输出最终实体集合
            outputFinalEntityCollection(entityData);

        } catch (Exception e) {
            System.err.println("设定验证失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 输出最终实体集合
     */
    private static void outputFinalEntityCollection(EntityData entityData) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("【最终实体集合构建完成】");
        System.out.println("=".repeat(80));

        // 按类型分组统计
        Map<String, List<EntityRegistry>> entitiesByType = new HashMap<>();
        for (EntityRegistry entity : entityData.entities.values()) {
            entitiesByType
                    .computeIfAbsent(entity.getEntityType(), k -> new ArrayList<>())
                    .add(entity);
        }

        // 输出统计信息
        System.out.println("\n=== 实体统计 ===");
        System.out.println("实体总数: " + entityData.entities.size());
        entitiesByType.forEach((type, list) -> System.out.println("  " + type + ": " + list.size() + "个"));

        // 按类型输出所有实体
        System.out.println("\n=== 实体详情 ===");

        for (Map.Entry<String, List<EntityRegistry>> entry : entitiesByType.entrySet()) {
            String type = entry.getKey();
            List<EntityRegistry> entities = entry.getValue();

            System.out.println("\n--- " + type + " (" + entities.size() + "个) ---");

            for (EntityRegistry entity : entities) {
                System.out.println("\n[" + entity.getEntityId() + "] " + entity.getStandardName());

                if (entity.getAliases() != null && !entity.getAliases().isEmpty()) {
                    System.out.println("  别名: " + String.join("、", entity.getAliases()));
                }

                // 输出上下文示例
                List<String> contexts = entityData.entityContexts.get(entity.getEntityId());
                if (contexts != null && !contexts.isEmpty()) {
                    System.out.println("  上下文示例: " + contexts.get(0).substring(0, Math.min(100, contexts.get(0).length())) + "...");
                }

                // 输出属性设定
                Map<String, Object> attrs = entityData.entityAttributes.get(entity.getEntityId());
                if (attrs != null && !attrs.isEmpty()) {
                    System.out.println("  设定:");
                    attrs.forEach((key, value) -> {
                        if (value != null && !value.toString().isEmpty()) {
                            String valStr = value.toString();
                            if (valStr.length() > 50) {
                                valStr = valStr.substring(0, 50) + "...";
                            }
                            System.out.println("    - " + key + ": " + valStr);
                        }
                    });
                }
            }
        }

        // 生成JSON格式的完整实体集合
        System.out.println("\n\n=== JSON格式实体集合 ===");
        try {
            String json = buildEntityCollectionJson(entityData);
            System.out.println(json);
        } catch (Exception e) {
            System.err.println("生成JSON失败: " + e.getMessage());
        }
    }

    /**
     * 构建JSON格式的实体集合
     */
    private static String buildEntityCollectionJson(EntityData entityData) throws Exception {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalEntities", entityData.entities.size());
        result.put("totalContexts", entityData.entityContexts.size());

        // 按类型分组
        Map<String, List<Map<String, Object>>> entitiesByType = new LinkedHashMap<>();
        for (EntityRegistry entity : entityData.entities.values()) {
            Map<String, Object> entityMap = new LinkedHashMap<>();
            entityMap.put("id", entity.getEntityId());
            entityMap.put("name", entity.getStandardName());
            entityMap.put("type", entity.getEntityType());
            if (entity.getAliases() != null && !entity.getAliases().isEmpty()) {
                entityMap.put("aliases", entity.getAliases());
            }

            // 添加上下文
            List<String> contexts = entityData.entityContexts.get(entity.getEntityId());
            if (contexts != null && !contexts.isEmpty()) {
                entityMap.put("contexts", contexts);
            }

            // 添加属性
            Map<String, Object> attrs = entityData.entityAttributes.get(entity.getEntityId());
            if (attrs != null && !attrs.isEmpty()) {
                entityMap.put("attributes", attrs);
            }

            entitiesByType
                    .computeIfAbsent(entity.getEntityType(), k -> new ArrayList<>())
                    .add(entityMap);
        }

        result.put("entitiesByType", entitiesByType);

        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
    }

    /**
     * 构建设定验证Prompt
     */
    private static String buildVerificationPrompt(String chapterText, EntityData entityData) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("""
            # 任务：验证实体设定是否符合原著

            ## 背景
            已从小说中提取了以下实体设定，请对照原文检查这些设定是否准确、是否有遗漏、是否有矛盾。

            ## 重要原则
            1. **严格对照原文**：每个设定都必须能在原文中找到依据
            2. **检查遗漏**：检查是否有重要实体/设定未被提取
            3. **检查矛盾**：检查不同实体设定之间是否有矛盾
            4. **精确引用**：引用原文时必须精确，不能概括或改写

            ## 已提取的实体设定

            """);

        // 格式化所有实体设定
        int count = 0;
        for (EntityRegistry entity : entityData.entities.values()) {
            count++;
            List<String> contexts = entityData.entityContexts.get(entity.getEntityId());
            Map<String, Object> attrs = entityData.entityAttributes.get(entity.getEntityId());

            prompt.append(count).append(". **").append(entity.getStandardName()).append("** (").append(entity.getEntityType()).append(")\n");
            if (entity.getAliases() != null && !entity.getAliases().isEmpty()) {
                prompt.append("   别名：").append(String.join("、", entity.getAliases())).append("\n");
            }
            if (attrs != null && !attrs.isEmpty()) {
                prompt.append("   设定：\n");
                attrs.forEach((key, value) -> {
                    if (value != null && !value.toString().isEmpty()) {
                        prompt.append("   - ").append(key).append(": ").append(value).append("\n");
                    }
                });
            }
            prompt.append("\n");
        }

        prompt.append("""

            ## 原文内容

            """);
        // 限制原文长度，避免超出token限制
        String truncatedText = chapterText.length() > 8000 ? chapterText.substring(0, 8000) + "..." : chapterText;
        prompt.append(truncatedText);

        prompt.append("""

            ## 输出要求

            请输出以下内容：

            1. **验证通过的实体**：哪些实体的设定与原文一致
            2. **需要修正的实体**：哪些实体的设定需要修正（引用原文说明）
            3. **遗漏的实体**：本章是否有重要实体/设定未被提取（引用原文）
            4. **发现的矛盾**：是否有设定之间存在矛盾
            5. **补充的身份规则**：如果是Identity类型，检查是否有规则/约束未被完整记录

            ## 输出格式

            ```json
            {
              "verified": ["实体名1", "实体名2"],
              "needsCorrection": [
                {
                  "entity": "实体名",
                  "issue": "问题描述",
                  "originalText": "原文引用",
                  "suggestedFix": "建议修正"
                }
              ],
              "missingEntities": [
                {
                  "name": "遗漏的实体名",
                  "type": "实体类型",
                  "context": "包含该实体的原文"
                }
              ],
              "contradictions": [],
              "supplementaryRules": [
                {
                  "identity": "身份名称",
                  "rule": "补充的规则",
                  "sourceText": "原文引用"
                }
              ]
            }
            ```

            如果本章没有特殊情况，返回：
            ```json
            {
              "verified": ["实体名1", "实体名2"],
              "needsCorrection": [],
              "missingEntities": [],
              "contradictions": [],
              "supplementaryRules": []
            }
            ```

            请直接返回JSON，不要添加其他说明。
            """);

        return prompt.toString();
    }

    /**
     * 构造实体设定生成Prompt - 基于原文上下文
     */
    private static String buildEntityProfilePrompt(EntityRegistry entity, List<String> contexts, Map<String, Object> attributes) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("# 任务：基于原文生成实体设定\n\n");
        prompt.append("请仅根据以下原文上下文，总结实体「").append(entity.getStandardName()).append("」的设定信息。\n\n");
        
        prompt.append("## 重要约束\n\n");
        prompt.append("1. **严格基于原文**：只能从下面的原文上下文中提取信息，绝对不能编造、想象或扩展\n");
        prompt.append("2. **保持简洁**：直接引用或概括原文中的描述，不要添加任何原文中没有的内容\n");
        prompt.append("3. **如果原文中没有相关信息**：就不要生成该部分，直接跳过\n\n");
        
        // 实体基本信息
        prompt.append("## 实体信息\n\n");
        prompt.append("- 名称：").append(entity.getStandardName()).append("\n");
        prompt.append("- 类型：").append(entity.getEntityType()).append("\n");
        if (entity.getAliases() != null && !entity.getAliases().isEmpty()) {
            prompt.append("- 别名：").append(String.join("、", entity.getAliases())).append("\n");
        }
        prompt.append("\n");
        
        // 原文上下文
        prompt.append("## 原文上下文\n\n");
        if (contexts != null && !contexts.isEmpty()) {
            for (int i = 0; i < contexts.size(); i++) {
                prompt.append(i + 1).append(". ").append(contexts.get(i)).append("\n\n");
            }
        } else {
            prompt.append("（无原文上下文）\n\n");
        }
        
        // LLM已提取的属性（仅供参考）
        if (attributes != null && !attributes.isEmpty()) {
            prompt.append("## LLM已提取属性（仅供参考）\n\n");
            attributes.forEach((key, value) -> {
                if (value != null && !value.toString().isEmpty()) {
                    prompt.append("- ").append(key).append(": ").append(value).append("\n");
                }
            });
            prompt.append("\n");
        }
        
        prompt.append("## 输出要求\n\n");
        
        // 根据实体类型生成不同的设定模板
        String entityType = entity.getEntityType();
        if (entityType == null) {
            entityType = "ENTITY";
        }
        
        switch (entityType) {
            case "Character" -> {
                prompt.append("请总结以下内容（只总结原文中出现的信息）：\n");
                prompt.append("1. **外貌描述**：原文中对角色外貌、着装的描述\n");
                prompt.append("2. **性格特点**：原文中体现的性格、行为方式\n");
                prompt.append("3. **背景信息**：原文中提到的背景、经历\n");
                prompt.append("4. **能力技能**：原文中出现的能力、技能\n");
                prompt.append("5. **人物关系**：原文中与其他角色的关系\n");
            }
            case "Location" -> {
                prompt.append("请总结以下内容（只总结原文中出现的信息）：\n");
                prompt.append("1. **地点描述**：原文中对地点的描述\n");
                prompt.append("2. **地理位置**：原文中提到的位置信息\n");
                prompt.append("3. **相关事件**：原文中发生在该地点的事件\n");
            }
            case "Organization" -> {
                prompt.append("请总结以下内容（只总结原文中出现的信息）：\n");
                prompt.append("1. **组织介绍**：原文中对组织的介绍\n");
                prompt.append("2. **成员信息**：原文中提到的成员\n");
                prompt.append("3. **相关事件**：原文中与该组织相关的事件\n");
            }
            case "Item" -> {
                prompt.append("请总结以下内容（只总结原文中出现的信息）：\n");
                prompt.append("1. **物品描述**：原文中对物品的描述\n");
                prompt.append("2. **作用能力**：原文中提到的作用\n");
                prompt.append("3. **相关事件**：原文中与该物品相关的事件\n");
            }
            case "Skill" -> {
                prompt.append("请总结以下内容（只总结原文中出现的信息）：\n");
                prompt.append("1. **技能描述**：原文中对技能的描述\n");
                prompt.append("2. **使用方式**：原文中提到的使用方法\n");
                prompt.append("3. **效果表现**：原文中描述的效果\n");
            }
            case "Identity" -> {
                prompt.append("请总结以下内容（只总结原文中出现的信息）：\n");
                prompt.append("1. **身份定义**：该身份的定义和含义\n");
                prompt.append("2. **获得条件**：如何获得该身份\n");
                prompt.append("3. **身份规则**：该身份需要遵守的规则/约束（如有）\n");
                prompt.append("4. **特殊能力**：该身份赋予的能力/特权\n");
                prompt.append("5. **相关事件**：原文中与该身份相关的事件\n");
            }
            case "Rule" -> {
                prompt.append("请总结以下内容（只总结原文中出现的信息）：\n");
                prompt.append("1. **规则内容**：该规则的具体内容\n");
                prompt.append("2. **适用范围**：该规则适用的范围/对象\n");
                prompt.append("3. **约束惩罚**：违反规则的后果（如有）\n");
                prompt.append("4. **相关事件**：原文中与该规则相关的事件\n");
            }
            case "Era" -> {
                prompt.append("请总结以下内容（只总结原文中出现的信息）：\n");
                prompt.append("1. **时代背景**：该时代的历史背景\n");
                prompt.append("2. **主要特征**：该时代的显著特征\n");
                prompt.append("3. **代表势力**：该时代的代表势力或人物\n");
                prompt.append("4. **终结原因**：该时代结束的原因（如有）\n");
            }
            case "Species" -> {
                prompt.append("请总结以下内容（只总结原文中出现的信息）：\n");
                prompt.append("1. **种族特征**：该种族的外貌/特征\n");
                prompt.append("2. **种族能力**：该种族的天赋能力\n");
                prompt.append("3. **生活习性**：该种族的生活习性/社会形态\n");
                prompt.append("4. **与人类关系**：与人类/其他种族的关系\n");
            }
            case "Realm" -> {
                prompt.append("请总结以下内容（只总结原文中出现的信息）：\n");
                prompt.append("1. **境界描述**：该境界的描述\n");
                prompt.append("2. **境界标志**：突破该境界的标志/特征\n");
                prompt.append("3. **实力表现**：该境界对应的实力表现\n");
                prompt.append("4. **修炼难度**：该境界的修炼难度\n");
            }
            case "Constitution" -> {
                prompt.append("请总结以下内容（只总结原文中出现的信息）：\n");
                prompt.append("1. **体质描述**：该体质的描述\n");
                prompt.append("2. **特殊能力**：该体质带来的特殊能力\n");
                prompt.append("3. **修炼优势**：该体质对修炼的帮助\n");
                prompt.append("4. **已知拥有者**：原文中提到的拥有该体质的人物\n");
            }
            case "Currency" -> {
                prompt.append("请总结以下内容（只总结原文中出现的信息）：\n");
                prompt.append("1. **货币描述**：该货币/资源的描述\n");
                prompt.append("2. **获取方式**：如何获取该货币/资源\n");
                prompt.append("3. **使用场景**：该货币/资源的使用场景\n");
                prompt.append("4. **价值换算**：与其他货币/资源的兑换比例（如有）\n");
            }
            case "Concept" -> {
                prompt.append("请总结以下内容（只总结原文中出现的信息）：\n");
                prompt.append("1. **概念定义**：该抽象概念的定义\n");
                prompt.append("2. **运作机制**：该概念如何运作/影响\n");
                prompt.append("3. **获取/损失方式**：如何获取或损失该概念\n");
                prompt.append("4. **实际表现**：原文中该概念的具体表现\n");
            }
            case "Legacy" -> {
                prompt.append("请总结以下内容（只总结原文中出现的信息）：\n");
                prompt.append("1. **传承内容**：该传承包含的内容\n");
                prompt.append("2. **传承来源**：传承的来源/创始人\n");
                prompt.append("3. **继承条件**：如何继承该传承\n");
                prompt.append("4. **修炼效果**：继承该传承的效果\n");
            }
            case "Phenomenon" -> {
                prompt.append("请总结以下内容（只总结原文中出现的信息）：\n");
                prompt.append("1. **异象描述**：该天地异象的描述\n");
                prompt.append("2. **触发条件**：触发该异象的条件\n");
                prompt.append("3. **异象效果**：该异象的效果/影响\n");
                prompt.append("4. **相关记录**：原文中该异象的出现记录\n");
            }
            default -> {
                prompt.append("请总结原文中关于该实体的所有信息。\n");
            }
        }
        
        prompt.append("\n## 输出格式\n\n");
        prompt.append("请用简洁的语言输出，每个部分分段描述。\n");
        prompt.append("如果原文中没有某个方面的信息，就不要生成该部分。\n");
        prompt.append("不需要返回JSON格式，直接返回文本即可。\n");
        
        return prompt.toString();
    }

    /**
     * 文本分块
     */
    private static List<String> splitIntoChunks(String text, int chunkSize) {
        List<String> chunks = new ArrayList<>();
        int length = text.length();

        for (int i = 0; i < length; i += chunkSize) {
            int end = Math.min(i + chunkSize, length);
            chunks.add(text.substring(i, end));
        }

        return chunks;
    }

    /**
     * 生成实体ID
     */
    private static String generateEntityId(String type, int counter) {
        // 防御性检查：type为null时使用默认值
        if (type == null || type.isEmpty()) {
            type = "ENTITY";
        }
        
        String prefix = switch (type) {
            case "Character" -> "CHAR";
            case "Location" -> "LOC";
            case "Organization" -> "ORG";
            case "Item" -> "ITEM";
            case "Skill" -> "SKILL";
            default -> "ENTITY";
        };
        return String.format("%s_%03d", prefix, counter);
    }

    /**
     * 健壮的JSON解析方法 - 处理各种异常格式
     */
    private List<Map<String, Object>> parseJsonRobustly(String jsonContent) {
        // 方法0: 先清理可能的思考标签和markdown
        jsonContent = cleanThinkingTagsAndMarkdown(jsonContent);

        // 方法1: 直接尝试解析
        try {
            return objectMapper.readValue(jsonContent, new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            System.err.println("    直接解析失败，尝试修复...");
        }

        // 方法2: 智能提取JSON数组
        try {
            String extracted = extractJsonArraySmart(jsonContent);
            if (extracted != null && !extracted.isEmpty()) {
                System.err.println("    提取到JSON长度: " + extracted.length());
                return objectMapper.readValue(extracted, new TypeReference<List<Map<String, Object>>>() {});
            }
        } catch (Exception e) {
            System.err.println("    智能提取JSON失败: " + e.getMessage());
            System.err.println("    尝试从错误位置继续...");
        }

        // 方法3: 逐对象解析
        try {
            List<Map<String, Object>> result = parseJsonObjectsOneByOne(jsonContent);
            if (!result.isEmpty()) {
                return result;
            }
        } catch (Exception e) {
            System.err.println("    逐对象解析失败: " + e.getMessage());
        }

        // 方法4: 使用正则提取关键字段作为后备
        try {
            List<Map<String, Object>> result = extractEntitiesByRegex(jsonContent);
            if (!result.isEmpty()) {
                System.err.println("    通过正则提取到 " + result.size() + " 个实体");
                return result;
            }
        } catch (Exception e) {
            System.err.println("    正则提取失败: " + e.getMessage());
        }

        System.err.println("    所有JSON解析方法均失败");
        return null;
    }

    /**
     * 清理思考标签和markdown
     */
    private String cleanThinkingTagsAndMarkdown(String json) {
        // 移除思考标签
        json = json.replaceAll("(?s)<think>.*?</think>", "");
        json = json.replaceAll("(?s)<thought>.*?</thought>", "");

        // 移除markdown代码块标记
        json = json.replaceAll("```json\\s*", "").replaceAll("\\s*```", "");
        json = json.replaceAll("```\\s*", "").replaceAll("\\s*```", "");

        return json;
    }

    /**
     * 智能提取JSON数组 - 处理各种异常情况
     */
    private String extractJsonArraySmart(String json) {
        // 找到所有可能的 [ 和 ] 位置
        List<Integer> openBrackets = new ArrayList<>();
        List<Integer> closeBrackets = new ArrayList<>();

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '[') openBrackets.add(i);
            if (c == ']') closeBrackets.add(i);
        }

        // 尝试找到完整的JSON数组
        for (int i = openBrackets.size() - 1; i >= 0; i--) {
            int start = openBrackets.get(i);
            for (int j = 0; j < closeBrackets.size(); j++) {
                int end = closeBrackets.get(j);
                if (end > start && isValidJsonArray(json.substring(start, end + 1))) {
                    return json.substring(start, end + 1);
                }
            }
        }

        // 尝试匹配 {...} 对象模式
        return extractJsonObjects(json);
    }

    /**
     * 检查是否是有效的JSON数组
     */
    private boolean isValidJsonArray(String json) {
        if (!json.startsWith("[") || !json.endsWith("]")) {
            return false;
        }
        try {
            objectMapper.readValue(json, new TypeReference<List<Object>>() {});
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 提取JSON对象数组
     */
    private String extractJsonObjects(String json) {
        // 找最后一个 [...] 块
        int lastOpen = json.lastIndexOf('[');
        int lastClose = json.lastIndexOf(']');

        if (lastOpen >= 0 && lastClose > lastOpen) {
            String candidate = json.substring(lastOpen, lastClose + 1);
            // 检查是否包含对象
            if (candidate.contains("{") && candidate.contains("}")) {
                return candidate;
            }
        }

        // 找第一个 [...] 块
        int firstOpen = json.indexOf('[');
        int firstClose = json.indexOf(']', firstOpen);
        if (firstOpen >= 0 && firstClose > firstOpen) {
            String candidate = json.substring(firstOpen, firstClose + 1);
            if (candidate.contains("{") && candidate.contains("}")) {
                return candidate;
            }
        }

        return null;
    }

    /**
     * 逐对象解析JSON
     */
    private List<Map<String, Object>> parseJsonObjectsOneByOne(String json) {
        List<Map<String, Object>> result = new ArrayList<>();

        // 使用更宽松的正则匹配JSON对象
        Pattern objectPattern = Pattern.compile("\\{[^{}]*\\}", Pattern.DOTALL);
        Matcher matcher = objectPattern.matcher(json);

        while (matcher.find()) {
            String objStr = matcher.group();
            try {
                Map<String, Object> obj = objectMapper.readValue(objStr, new TypeReference<Map<String, Object>>() {});
                if (isValidEntity(obj)) {
                    result.add(obj);
                }
            } catch (Exception e) {
                // 解析失败，尝试提取关键字段
                Map<String, Object> partial = extractPartialEntity(objStr);
                if (partial != null && !partial.isEmpty()) {
                    result.add(partial);
                }
            }
        }

        return result;
    }

    /**
     * 检查是否是有效的实体对象
     */
    private boolean isValidEntity(Map<String, Object> obj) {
        return obj.containsKey("standardName") || obj.containsKey("entity") ||
               obj.containsKey("name") || obj.containsKey("entityType") ||
               obj.containsKey("type");
    }

    /**
     * 通过正则表达式提取实体信息（最后手段）
     */
    private List<Map<String, Object>> extractEntitiesByRegex(String json) {
        List<Map<String, Object>> result = new ArrayList<>();

        // 分割可能的实体块（使用 } 分割）
        String[] parts = json.split("\\}(?=\\s*[,]?\\s*\\{)");
        for (String part : parts) {
            if (!part.trim().isEmpty() && !part.trim().equals(",")) {
                String objStr = part.trim();
                if (!objStr.startsWith("{")) {
                    objStr = "{" + objStr;
                }
                if (!objStr.endsWith("}")) {
                    objStr = objStr + "}";
                }

                Map<String, Object> entity = extractPartialEntity(objStr);
                if (entity != null && entity.containsKey("standardName")) {
                    result.add(entity);
                }
            }
        }

        return result;
    }

    /**
     * 清理格式错误的JSON
     */
    private String cleanMalformedJson(String json) {
        // 移除思考标签
        json = json.replaceAll("(?s)<think>.*?</think>", "");
        json = json.replaceAll("(?s)<thought>.*?</thought>", "");

        // 移除markdown代码块标记
        json = json.replaceAll("```json\\s*", "").replaceAll("\\s*```", "");

        // 修复控制字符
        json = json.replaceAll("[\n\r\t]", " ");

        // 移除多余逗号
        json = json.replaceAll(",(\\s*[}\\]])", "$1");

        // 修复不完整的JSON - 如果末尾有截断，尝试找到完整的对象/数组
        json = fixIncompleteJson(json);

        return json.trim();
    }

    /**
     * 修复不完整的JSON
     */
    private String fixIncompleteJson(String json) {
        // 统计括号数量
        int braceCount = 0;
        int bracketCount = 0;

        for (char c : json.toCharArray()) {
            if (c == '{') braceCount++;
            else if (c == '}') braceCount--;
            else if (c == '[') bracketCount++;
            else if (c == ']') bracketCount--;
        }

        // 补全缺失的括号
        while (braceCount > 0) {
            json += "}";
            braceCount--;
        }
        while (bracketCount > 0) {
            json += "]";
            bracketCount--;
        }

        return json;
    }

    /**
     * 松散解析JSON - 尝试处理各种异常
     */
    private List<Map<String, Object>> parseJsonLenient(String json) {
        List<Map<String, Object>> result = new ArrayList<>();

        // 尝试提取每个实体对象
        Pattern entityPattern = Pattern.compile("\\{[^{}]*\\}");
        Matcher matcher = entityPattern.matcher(json);

        while (matcher.find()) {
            String entityJson = matcher.group();
            try {
                Map<String, Object> entity = objectMapper.readValue(entityJson, new TypeReference<Map<String, Object>>() {});
                if (entity.containsKey("standardName") || entity.containsKey("entity")) {
                    result.add(entity);
                }
            } catch (Exception e) {
                // 单个实体解析失败，尝试提取关键字段
                Map<String, Object> partial = extractPartialEntity(entityJson);
                if (partial != null && !partial.isEmpty()) {
                    result.add(partial);
                }
            }
        }

        return result;
    }

    /**
     * 从损坏的JSON中提取部分实体信息
     */
    private Map<String, Object> extractPartialEntity(String json) {
        Map<String, Object> entity = new HashMap<>();

        // 提取 standardName - 支持单引号和双引号
        Pattern namePattern = Pattern.compile("[\"']?standardName[\"']?\\s*[:＝]\\s*[\"']([^\"']+)[\"']");
        Matcher matcher = namePattern.matcher(json);
        if (matcher.find()) {
            entity.put("standardName", matcher.group(1).trim());
        }

        // 提取 entityType
        Pattern typePattern = Pattern.compile("[\"']?entityType[\"']?\\s*[:＝]\\s*[\"']([^\"']+)[\"']");
        matcher = typePattern.matcher(json);
        if (matcher.find()) {
            entity.put("entityType", matcher.group(1).trim());
        }

        // 提取 type
        Pattern typePattern2 = Pattern.compile("[\"']?type[\"']?\\s*[:＝]\\s*[\"']([^\"']+)[\"']");
        matcher = typePattern2.matcher(json);
        if (matcher.find()) {
            entity.put("type", matcher.group(1).trim());
        }

        // 提取 aliases - 支持数组格式 [item1, item2]
        Pattern aliasPattern = Pattern.compile("[\"']?aliases[\"']?\\s*[:＝]\\s*\\[([^\\]]*)\\]");
        matcher = aliasPattern.matcher(json);
        if (matcher.find()) {
            String aliasesStr = matcher.group(1);
            List<String> aliases = new ArrayList<>();
            for (String alias : aliasesStr.split(",")) {
                alias = alias.replaceAll("[\"']", "").trim();
                if (!alias.isEmpty()) {
                    aliases.add(alias);
                }
            }
            if (!aliases.isEmpty()) {
                entity.put("aliases", aliases);
            }
        }

        // 提取 firstMention
        Pattern mentionPattern = Pattern.compile("[\"']?firstMention[\"']?\\s*[:＝]\\s*[\"']?([0-9]+)[\"']?");
        matcher = mentionPattern.matcher(json);
        if (matcher.find()) {
            entity.put("firstMention", Integer.parseInt(matcher.group(1)));
        }

        // 提取 contexts
        Pattern contextsPattern = Pattern.compile("[\"']?contexts[\"']?\\s*[:＝]\\s*\\[([^\\]]*)\\]");
        matcher = contextsPattern.matcher(json);
        if (matcher.find()) {
            String contextsStr = matcher.group(1);
            List<String> contexts = new ArrayList<>();
            for (String ctx : contextsStr.split(",")) {
                ctx = ctx.replaceAll("[\"']", "").trim();
                if (!ctx.isEmpty()) {
                    contexts.add(ctx);
                }
            }
            if (!contexts.isEmpty()) {
                entity.put("contexts", contexts);
            }
        }

        // 提取 attributes（尝试匹配简单属性）
        Pattern attrsPattern = Pattern.compile("[\"']?attributes[\"']?\\s*[:＝]\\s*\\{([^\\}]*)\\}");
        matcher = attrsPattern.matcher(json);
        if (matcher.find()) {
            String attrsStr = matcher.group(1);
            Map<String, String> attrs = new HashMap<>();
            String[] attrPairs = attrsStr.split(",");
            for (String pair : attrPairs) {
                String[] kv = pair.split("[:＝]", 2);
                if (kv.length == 2) {
                    String key = kv[0].replaceAll("[\"']", "").trim();
                    String value = kv[1].replaceAll("[\"']", "").trim();
                    if (!key.isEmpty() && !value.isEmpty()) {
                        attrs.put(key, value);
                    }
                }
            }
            if (!attrs.isEmpty()) {
                entity.put("attributes", attrs);
            }
        }

        return entity.isEmpty() ? null : entity;
    }

    /**
     * 修复JSON格式问题 - 综合解决方案
     */
    private static String fixJsonFormat(String json) {
        if (json == null || json.isEmpty()) {
            return json;
        }
        
        // 第一步：替换中文标点符号
        json = json.replace("“", "\"");
        json = json.replace("”", "\"");
        json = json.replace("‘", "'")
.replace("’", "'");
        json = json.replace("：", ":");
        json = json.replace("，", ",");
        json = json.replace("｛", "{");
        json = json.replace("｝", "}");
        json = json.replace("［", "[");
        json = json.replace("］", "]");
        
        // 第二步：将字符串值中的引号替换为单引号
        json = replaceQuotesInStrings(json);
        
        return json;
    }
    
    /**
     * 将JSON字符串值中的双引号替换为单引号
     */
    private static String replaceQuotesInStrings(String json) {
        StringBuilder result = new StringBuilder(json.length() * 2);
        int i = 0;
        
        while (i < json.length()) {
            char c = json.charAt(i);
            
            // 跳过结构字符
            if (c == '{' || c == '}' || c == '[' || c == ']' || c == ':' || c == ',') {
                result.append(c);
                i++;
                continue;
            }
            
            // 跳过空白
            if (Character.isWhitespace(c)) {
                result.append(c);
                i++;
                continue;
            }
            
            // 处理字符串
            if (c == '"') {
                result.append(c);
                i++;
                i = replaceQuotesInStringValue(json, i, result);
            } else {
                result.append(c);
                i++;
            }
        }
        
        return result.toString();
    }
    
    /**
     * 处理字符串值内容 - 将引号替换为单引号
     */
    private static int replaceQuotesInStringValue(String json, int start, StringBuilder result) {
        int i = start;
        
        while (i < json.length()) {
            char ch = json.charAt(i);
            
            // 处理转义序列
            if (ch == '\\') {
                // 检查是否是转义的引号
                if (i + 1 < json.length() && json.charAt(i + 1) == '"') {
                    // 将 \\" 替换为单引号
                    result.append("'");
                    i += 2;
                    continue;
                } else {
                    // 其他转义字符保留
                    result.append(ch);
                    i++;
                    if (i < json.length()) {
                        result.append(json.charAt(i));
                        i++;
                    }
                }
                continue;
            }
            
            // 遇到引号
            if (ch == '"') {
                // 查找下一个非空白字符
                int next = i + 1;
                while (next < json.length() && Character.isWhitespace(json.charAt(next))) {
                    next++;
                }
                
                if (next >= json.length()) {
                    // 到末尾，字符串结束
                    result.append(ch);
                    i++;
                    break;
                }
                
                char nextChar = json.charAt(next);
                if (nextChar == ',' || nextChar == ':' || nextChar == '}' || nextChar == ']') {
                    // 字符串结束
                    result.append(ch);
                    i++;
                    break;
                } else {
                    // 内容中的双引号，替换为单引号
                    result.append("'");
                    i++;
                }
            } else {
                result.append(ch);
                i++;
            }
        }
        
        return i;
    }

    public static String removeThinkingTags(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        // 去除XML格式的thinking标签及其内容
        String result = input.replaceAll("<thinking>.*?</thinking>", "");

        // 去除简化格式的thinking标签及其内容
        result = result.replaceAll("(?s)<think>.*?</think>", "");

        // 清理多余的空白字符
        result = result.replaceAll("\\s+", " ").trim();

        return result;
    }
}
