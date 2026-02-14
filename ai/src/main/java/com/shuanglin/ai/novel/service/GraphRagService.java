package com.shuanglin.ai.novel.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shuanglin.ai.langchain4j.assistant.DecomposeAssistant;
import com.shuanglin.ai.utils.FileReadUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;

/**
 * 图
 * 负责谱RAG服务基于RAG检索的事件识别（阶段二）
 */
@Slf4j
@Service
public class GraphRagService {

    private static final String NEO4J_URI = "bolt://8.138.204.38:7687";
    private static final String NEO4J_USER = "neo4j";
    private static final String NEO4J_PASSWORD = "password";

    @Autowired
    private DecomposeAssistant decomposeAssistant;

    @Autowired
    private NovelEntityService novelEntityService;

    private final Driver driver;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GraphRagService() {
        this.driver = GraphDatabase.driver(NEO4J_URI, AuthTokens.basic(NEO4J_USER, NEO4J_PASSWORD));
    }

    /**
     * 处理全书事件（批量处理）
     * @param bookUuid 书籍UUID
     * @param epubPath EPUB文件路径
     * @return 事件处理报告
     */
    public EventProcessReport processAllChapters(String bookUuid, String epubPath) {
        log.info("【阶段二】开始处理全书事件，书籍UUID: {}", bookUuid);

        long startTime = System.currentTimeMillis();
        EventProcessReport report = new EventProcessReport(bookUuid);

        // 1. 读取EPUB文件
        List<FileReadUtil.ParseResult> parseResults = FileReadUtil.readEpubFile(new File(epubPath));
        report.setTotalChapters(parseResults.size());

        // 2. 获取实体注册表
        String entityRegistryJson = novelEntityService.getEntityRegistryJson(bookUuid);

        // 3. 遍历每章提取事件
        for (int chapterIdx = 3; chapterIdx < parseResults.size(); chapterIdx++) {
            FileReadUtil.ParseResult chapter = parseResults.get(chapterIdx);
            String chapterText = String.join("\n", chapter.getContentList());
            int chapterIndex = chapterIdx + 1;

            // 准备上下文
            String lastContext = chapterIdx > 0
                    ? String.join("\n", parseResults.get(chapterIdx - 1).getContentList())
                    : "";
            String nextContext = chapterIdx < parseResults.size() - 1
                    ? String.join("\n", parseResults.get(chapterIdx + 1).getContentList())
                    : "";

            try {
                // 调用LLM提取事件
                String cypher = extractEventsWithRAG(chapterText, lastContext, nextContext,
                        chapter.getTitle(), chapterIndex, entityRegistryJson);

                if (cypher != null && !cypher.trim().isEmpty()) {
                    // 清理Markdown代码块
                    cypher = cleanMarkdownCodeBlock(cypher);

                    // 验证并执行Cypher
                    if (validateCypher(cypher)) {
                        executeCypher(cypher, bookUuid);
                        report.incrementSuccessChapters();
                        log.info("✅ 第{}章事件处理成功", chapterIndex);
                    } else {
                        report.incrementFailedChapters();
                        log.warn("⚠️ 第{}章Cypher验证失败", chapterIndex);
                    }
                } else {
                    report.incrementSkippedChapters();
                    log.info("第{}章无新事件", chapterIndex);
                }

            } catch (Exception e) {
                report.incrementFailedChapters();
                log.error("❌ 第{}章事件处理失败: {}", chapterIndex, e.getMessage());
            }
        }

        long endTime = System.currentTimeMillis();
        report.setTotalDuration(endTime - startTime);

        log.info("事件处理完成，成功: {}, 失败: {}, 跳过: {}",
                report.getSuccessChapters(),
                report.getFailedChapters(),
                report.getSkippedChapters());

        return report;
    }

    /**
     * 基于RAG的实体感知事件提取
     * @param chapterText 当前章节文本
     * @param lastContext 上一章文本
     * @param nextContext 下一章文本
     * @param chapterTitle 章节标题
     * @param chapterIndex 章节索引
     * @param entityRegistryJson 实体注册表JSON
     * @return Cypher语句
     */
    public String extractEventsWithRAG(String chapterText, String lastContext, String nextContext,
                                       String chapterTitle, int chapterIndex, String entityRegistryJson) {
        // 构建User Prompt
        String userPrompt = buildEventExtractionPrompt(chapterText, lastContext, nextContext,
                chapterTitle, chapterIndex, entityRegistryJson);

        // 调用LLM生成Cypher
        return decomposeAssistant.generateCypher(userPrompt);
    }

    /**
     * 构建事件提取的User Prompt
     */
    private String buildEventExtractionPrompt(String chapterText, String lastContext, String nextContext,
                                              String chapterTitle, int chapterIndex, String entityRegistryJson) {
        return String.format("""
                ## 当前任务
                请基于SystemPrompt中定义的强制性约束规则，处理以下输入：

                【章节信息】
                - 章节标题：%s
                - 章节索引：%d

                【实体注册表】
                以下是本书的实体标准库（包含所有已识别的实体及其别名），请使用entityId引用实体：
                %s

                【文本内容】
                lastContext（上一章完整内容）：
                %s

                作用：确认实体一致性、推断前置状态，**不提取新信息**

                ---

                indexText（当前章完整内容）：
                %s

                作用：**唯一的信息提取来源**，所有Cypher必须基于此生成

                ---

                nextContext（下一章完整内容）：
                %s

                作用：消除歧义、理解语境，**不生成Cypher**

                【关键约束】
                - Event.chapterIndex 必须使用：%d
                - Event.source 格式：第%d章 %s
                - Event节点不包含paragraphIndex属性
                - State.valid_from_chapter 必须等于Event.chapterIndex
                - State.valid_to_chapter 在状态转换时必须设置为新Event.chapterIndex
                - **重要**：必须使用实体注册表中的entityId来引用实体

                请严格遵循SystemPrompt的RULE-1至RULE-6，生成符合规范的Cypher语句。

                **输出规范：**
                1. ⚠️ **禁止Markdown代码块！** 不允许使用```cypher```或```包裹，直接输出Cypher语句
                2. 禁止输出任何自然语言解释
                3. 如indexText无新信息，必须返回空字符串
                4. 使用MERGE保证幂等性
                5. 节点标签使用双标签：:Entity:Character, :Event:StoryEvent
                """,
                chapterTitle,
                chapterIndex,
                entityRegistryJson,
                lastContext.length() > 2000 ? lastContext.substring(0, 2000) : lastContext,
                chapterText,
                nextContext.length() > 2000 ? nextContext.substring(0, 2000) : nextContext,
                chapterIndex,
                chapterIndex,
                chapterTitle
        );
    }

    /**
     * 从Neo4j查询实体的当前状态
     * @param bookUuid 书籍UUID
     * @param entityName 实体名称
     * @return 状态列表
     */
    public List<Map<String, Object>> getEntityCurrentState(String bookUuid, String entityName) {
        String cypher = String.format("""
                MATCH (e:Entity {bookUuid: '%s', name: '%s'})-[:CURRENT_STATE]->(s:State)
                RETURN s.stateType as stateType, s.stateValue as stateValue,
                       s.valid_from_chapter as fromChapter, s.valid_to_chapter as toChapter
                ORDER BY s.valid_from_chapter DESC
                """, bookUuid, entityName);

        try (Session session = driver.session()) {
            Result result = session.run(cypher);
            List<Map<String, Object>> states = new ArrayList<>();
            while (result.hasNext()) {
                org.neo4j.driver.Record record = result.next();
                Map<String, Object> state = new HashMap<>();
                state.put("stateType", record.get("stateType").asString());
                state.put("stateValue", record.get("stateValue").asString());
                state.put("fromChapter", record.get("fromChapter").asInt());
                if (record.get("toChapter").isNull()) {
                    state.put("toChapter", null);
                } else {
                    state.put("toChapter", record.get("toChapter").asInt());
                }
                states.add(state);
            }
            return states;
        } catch (Exception e) {
            log.error("查询实体状态失败: {}", entityName, e);
            return Collections.emptyList();
        }
    }

    /**
     * 查询实体的历史事件
     * @param bookUuid 书籍UUID
     * @param entityName 实体名称
     * @return 事件列表
     */
    public List<Map<String, Object>> getEntityHistoryEvents(String bookUuid, String entityName) {
        String cypher = String.format("""
                MATCH (e:Entity {bookUuid: '%s', name: '%s'})-[:PARTICIPATED_IN]->(ev:Event)
                OPTIONAL MATCH (ev)-[:GENERATES|TERMINATES|OBSERVES]->(target)
                RETURN ev.chapterIndex as chapterIndex, ev.eventType as eventType,
                       ev.description as description, ev.source as source,
                       target.name as targetEntity, type(ev) as relationType
                ORDER BY ev.chapterIndex ASC
                """, bookUuid, entityName);

        try (Session session = driver.session()) {
            Result result = session.run(cypher);
            List<Map<String, Object>> events = new ArrayList<>();
            while (result.hasNext()) {
                org.neo4j.driver.Record record = result.next();
                Map<String, Object> event = new HashMap<>();
                event.put("chapterIndex", record.get("chapterIndex").asInt());
                event.put("eventType", record.get("eventType").asString());
                event.put("description", record.get("description").asString());
                event.put("source", record.get("source").asString());
                event.put("targetEntity", record.get("targetEntity").isNull() ? null : record.get("targetEntity").asString());
                event.put("relationType", record.get("relationType").asString());
                events.add(event);
            }
            return events;
        } catch (Exception e) {
            log.error("查询实体历史事件失败: {}", entityName, e);
            return Collections.emptyList();
        }
    }

    /**
     * 查询两个实体之间的关系
     * @param bookUuid 书籍UUID
     * @param entity1Name 实体1名称
     * @param entity2Name 实体2名称
     * @return 关系列表
     */
    public List<Map<String, Object>> getEntityRelations(String bookUuid, String entity1Name, String entity2Name) {
        String cypher = String.format("""
                MATCH (e1:Entity {bookUuid: '%s', name: '%s'})-[r]-(e2:Entity {name: '%s'})
                RETURN type(r) as relationType, e1.name as source, e2.name as target,
                       e1.entityType as sourceType, e2.entityType as targetType
                """, bookUuid, entity1Name, entity2Name);

        try (Session session = driver.session()) {
            Result result = session.run(cypher);
            List<Map<String, Object>> relations = new ArrayList<>();
            while (result.hasNext()) {
                org.neo4j.driver.Record record = result.next();
                Map<String, Object> relation = new HashMap<>();
                relation.put("relationType", record.get("relationType").asString());
                relation.put("source", record.get("source").asString());
                relation.put("target", record.get("target").asString());
                relations.add(relation);
            }
            return relations;
        } catch (Exception e) {
            log.error("查询实体关系失败: {} - {}", entity1Name, entity2Name, e);
            return Collections.emptyList();
        }
    }

    /**
     * 清理Markdown代码块
     */
    private String cleanMarkdownCodeBlock(String cypher) {
        if (cypher == null) {
            return null;
        }

        String cleaned = cypher.trim();

        if (cleaned.startsWith("```cypher")) {
            cleaned = cleaned.substring(9).trim();
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3).trim();
        }

        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3).trim();
        }

        return cleaned;
    }

    /**
     * 验证Cypher语句
     */
    private boolean validateCypher(String cypher) {
        if (cypher == null || cypher.trim().isEmpty()) {
            return false;
        }

        // 验证Event节点不应包含paragraphIndex属性
        if (cypher.contains("paragraphIndex:")) {
            log.warn("验证失败：Event节点不应包含paragraphIndex属性");
            return false;
        }

        return true;
    }

    /**
     * 执行Cypher语句
     */
    private void executeCypher(String cypher, String bookUuid) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                String[] statements = cypher.split(";\\s*(?=CREATE|MERGE|MATCH)");
                for (String statement : statements) {
                    if (statement != null && !statement.trim().isEmpty()) {
                        tx.run(statement.trim());
                    }
                }
                return null;
            });
        } catch (Exception e) {
            log.error("Cypher执行失败", e);
            throw e;
        }
    }

    /**
     * 事件处理报告
     */
    @Data
    public static class EventProcessReport {
        private String bookUuid;
        private int totalChapters;
        private int successChapters;
        private int failedChapters;
        private int skippedChapters;
        private long totalDuration;

        public EventProcessReport(String bookUuid) {
            this.bookUuid = bookUuid;
        }

        public void incrementSuccessChapters() {
            this.successChapters++;
        }

        public void incrementFailedChapters() {
            this.failedChapters++;
        }

        public void incrementSkippedChapters() {
            this.skippedChapters++;
        }
    }
}
