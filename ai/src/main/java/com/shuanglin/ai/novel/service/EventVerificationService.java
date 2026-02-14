package com.shuanglin.ai.novel.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shuanglin.ai.langchain4j.assistant.DecomposeAssistant;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 事件验证服务
 * 负责阶段三的事件-实体关联验证和时序一致性验证
 */
@Slf4j
@Service
public class EventVerificationService {

    private static final String NEO4J_URI = "bolt://8.138.204.38:7687";
    private static final String NEO4J_USER = "neo4j";
    private static final String NEO4J_PASSWORD = "password";

    @Autowired
    private DecomposeAssistant decomposeAssistant;

    @Autowired
    private NovelEntityService novelEntityService;

    private final Driver driver;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public EventVerificationService() {
        this.driver = GraphDatabase.driver(NEO4J_URI, AuthTokens.basic(NEO4J_USER, NEO4J_PASSWORD));
    }

    /**
     * 验证事件-实体关联的合理性
     * @param bookUuid 书籍UUID
     * @return 验证报告
     */
    public EventVerificationReport verifyEventEntityRelations(String bookUuid) {
        log.info("【阶段三】开始验证事件-实体关联，书籍UUID: {}", bookUuid);

        long startTime = System.currentTimeMillis();
        EventVerificationReport report = new EventVerificationReport(bookUuid);

        // 1. 获取Neo4j中的所有事件
        List<Map<String, Object>> events = fetchAllEvents(bookUuid);
        report.setTotalEvents(events.size());

        // 2. 获取实体注册表
        String entityRegistryJson = novelEntityService.getEntityRegistryJson(bookUuid);

        // 3. 批量验证（每批100个事件）
        int batchSize = 100;
        for (int i = 0; i < events.size(); i += batchSize) {
            int end = Math.min(i + batchSize, events.size());
            List<Map<String, Object>> batch = events.subList(i, end);

            try {
                String eventListJson = objectMapper.writeValueAsString(batch);
                String verificationResult = decomposeAssistant.verifyEvents(eventListJson, entityRegistryJson);

                List<Map<String, Object>> results = objectMapper.readValue(
                        verificationResult,
                        new TypeReference<List<Map<String, Object>>>() {}
                );

                for (Map<String, Object> result : results) {
                    boolean isValid = (Boolean) result.getOrDefault("isValid", true);
                    double confidence = (Double) result.getOrDefault("confidence", 1.0);

                    if (isValid) {
                        report.incrementValidEvents();
                    } else {
                        report.incrementInvalidEvents();
                        report.addEventIssue((String) result.get("eventUuid"),
                                (List<String>) result.getOrDefault("issues", Collections.emptyList()));
                    }

                    // 更新置信度
                    String eventUuid = (String) result.get("eventUuid");
                    if (eventUuid != null && confidence < 0.8) {
                        updateEventConfidence(eventUuid, confidence);
                    }
                }

            } catch (Exception e) {
                log.error("批次事件验证失败: {}-{}", i, end, e);
            }
        }

        long endTime = System.currentTimeMillis();
        report.setVerificationDuration(endTime - startTime);

        log.info("事件验证完成，有效: {}, 无效: {}, 耗时: {}ms",
                report.getValidEvents(),
                report.getInvalidEvents(),
                report.getVerificationDuration());

        return report;
    }

    /**
     * 验证事件时序一致性
     * @param bookUuid 书籍UUID
     * @return 时序验证报告
     */
    public TemporalVerificationReport verifyTemporalConsistency(String bookUuid) {
        log.info("【阶段三】开始验证事件时序一致性，书籍UUID: {}", bookUuid);

        long startTime = System.currentTimeMillis();
        TemporalVerificationReport report = new TemporalVerificationReport(bookUuid);

        // 1. 获取所有事件
        List<Map<String, Object>> events = fetchAllEvents(bookUuid);

        // 2. 按章节排序
        events.sort(Comparator.comparingInt(e -> (Integer) e.getOrDefault("chapterIndex", 0)));

        // 3. 检查时序一致性
        Map<String, Integer> entityLastChapter = new HashMap<>();
        for (Map<String, Object> event : events) {
            int chapterIndex = (Integer) event.getOrDefault("chapterIndex", 0);
            String eventType = (String) event.getOrDefault("eventType", "");
            List<String> participants = (List<String>) event.getOrDefault("participants", Collections.emptyList());

            // 检查状态变化事件的时序
            if (isStateChangeEvent(eventType)) {
                for (String entity : participants) {
                    Integer lastChapter = entityLastChapter.get(entity);
                    if (lastChapter != null && chapterIndex < lastChapter) {
                        // 发现时序问题
                        TemporalIssue issue = new TemporalIssue();
                        issue.setEntity(entity);
                        issue.setEventChapter(chapterIndex);
                        issue.setLastChapter(lastChapter);
                        issue.setIssueType("STATE_REGRESSION");
                        issue.setDescription(String.format("实体 %s 在第%d章发生状态变化，但之前最后出现是在第%d章",
                                entity, chapterIndex, lastChapter));
                        report.addIssue(issue);
                        report.incrementIssues();
                    }
                    entityLastChapter.put(entity, chapterIndex);
                }
            } else {
                // 更新实体的最后出现章节
                for (String entity : participants) {
                    Integer lastChapter = entityLastChapter.get(entity);
                    if (lastChapter == null || chapterIndex > lastChapter) {
                        entityLastChapter.put(entity, chapterIndex);
                    }
                }
            }
        }

        long endTime = System.currentTimeMillis();
        report.setVerificationDuration(endTime - startTime);
        report.setTotalEvents(events.size());

        log.info("时序验证完成，发现{}个问题，耗时: {}ms",
                report.getIssues(),
                report.getVerificationDuration());

        return report;
    }

    /**
     * 验证事件因果关系
     * @param bookUuid 书籍UUID
     * @return 因果验证报告
     */
    public CausalityVerificationReport verifyCausality(String bookUuid) {
        log.info("【阶段三】开始验证事件因果关系，书籍UUID: {}", bookUuid);

        long startTime = System.currentTimeMillis();
        CausalityVerificationReport report = new CausalityVerificationReport(bookUuid);

        // 1. 获取所有事件
        List<Map<String, Object>> events = fetchAllEvents(bookUuid);

        // 2. 按章节排序
        events.sort(Comparator.comparingInt(e -> (Integer) e.getOrDefault("chapterIndex", 0)));

        // 3. 检查因果链
        Map<String, List<Map<String, Object>>> eventChains = new HashMap<>();
        for (Map<String, Object> event : events) {
            String eventType = (String) event.getOrDefault("eventType", "");

            // 构建因果链
            if ("BREAKTHROUGH".equals(eventType)) {
                // 突破事件前应该有修炼事件
                int chapterIndex = (Integer) event.getOrDefault("chapterIndex", 0);
                boolean hasPreviousCultivation = findEventBefore(events, chapterIndex, "CULTIVATION");
                if (!hasPreviousCultivation) {
                    report.addMissingPredecessor(event, "CULTIVATION");
                }
            } else if ("BATTLE".equals(eventType)) {
                // 战斗事件前可能有修炼或相遇事件
                int chapterIndex = (Integer) event.getOrDefault("chapterIndex", 0);
                boolean hasPredecessor = findEventBefore(events, chapterIndex,
                        List.of("CULTIVATION", "ENCOUNTER", "DECISION"));
                if (!hasPredecessor) {
                    // 记录但不作为问题
                    report.addWeakCausality(event);
                }
            }
        }

        long endTime = System.currentTimeMillis();
        report.setVerificationDuration(endTime - startTime);

        log.info("因果验证完成，缺失前置事件: {}, 弱因果关系: {}, 耗时: {}ms",
                report.getMissingPredecessors().size(),
                report.getWeakCausalities().size(),
                report.getVerificationDuration());

        return report;
    }

    /**
     * 从Neo4j获取所有事件
     */
    private List<Map<String, Object>> fetchAllEvents(String bookUuid) {
        String cypher = String.format("""
                MATCH (e:Event {bookUuid: '%s'})
                OPTIONAL MATCH (e)-[:PARTICIPATED_IN]-(entity:Entity)
                RETURN e.uuid as uuid, e.chapterIndex as chapterIndex, e.eventType as eventType,
                       e.description as description, e.confidence as confidence,
                       collect(DISTINCT entity.name) as participants
                ORDER BY e.chapterIndex
                """, bookUuid);

        try (Session session = driver.session()) {
            Result result = session.run(cypher);
            List<Map<String, Object>> events = new ArrayList<>();

            while (result.hasNext()) {
                org.neo4j.driver.Record record = result.next();
                Map<String, Object> event = new HashMap<>();
                event.put("uuid", record.get("uuid").asString());
                event.put("chapterIndex", record.get("chapterIndex").asInt());
                event.put("eventType", record.get("eventType").asString());
                event.put("description", record.get("description").asString());
                event.put("confidence", record.get("confidence").asDouble());

                List<Object> participants = record.get("participants").asList();
                event.put("participants", participants.stream()
                        .map(Object::toString)
                        .collect(Collectors.toList()));

                events.add(event);
            }

            return events;

        } catch (Exception e) {
            log.error("获取事件失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 更新事件置信度
     */
    private void updateEventConfidence(String eventUuid, double confidence) {
        String cypher = String.format("""
                MATCH (e:Event {uuid: '%s'})
                SET e.confidence = %f
                """, eventUuid, confidence);

        try (Session session = driver.session()) {
            session.run(cypher);
        } catch (Exception e) {
            log.error("更新事件置信度失败: {}", eventUuid, e);
        }
    }

    /**
     * 判断是否为状态变化事件
     */
    private boolean isStateChangeEvent(String eventType) {
        return List.of("BREAKTHROUGH", "CULTIVATION", "ACQUISITION", "MOVEMENT")
                .contains(eventType);
    }

    /**
     * 查找指定类型的前置事件
     */
    private boolean findEventBefore(List<Map<String, Object>> events, int chapterIndex, String eventType) {
        return findEventBefore(events, chapterIndex, List.of(eventType));
    }

    /**
     * 查找指定类型列表的前置事件
     */
    private boolean findEventBefore(List<Map<String, Object>> events, int chapterIndex, List<String> eventTypes) {
        return events.stream()
                .filter(e -> {
                    int ec = (Integer) e.getOrDefault("chapterIndex", 0);
                    String et = (String) e.getOrDefault("eventType", "");
                    return ec < chapterIndex && eventTypes.contains(et);
                })
                .findAny()
                .isPresent();
    }

    /**
     * 事件验证报告
     */
    @Data
    public static class EventVerificationReport {
        private String bookUuid;
        private int totalEvents;
        private int validEvents;
        private int invalidEvents;
        private long verificationDuration;
        private Map<String, List<String>> eventIssues = new HashMap<>();

        public EventVerificationReport(String bookUuid) {
            this.bookUuid = bookUuid;
        }

        public void incrementValidEvents() {
            this.validEvents++;
        }

        public void incrementInvalidEvents() {
            this.invalidEvents++;
        }

        public void addEventIssue(String eventUuid, List<String> issues) {
            this.eventIssues.put(eventUuid, issues);
        }
    }

    /**
     * 时序验证报告
     */
    @Data
    public static class TemporalVerificationReport {
        private String bookUuid;
        private int totalEvents;
        private int issues;
        private long verificationDuration;
        private List<TemporalIssue> temporalIssues = new ArrayList<>();

        public TemporalVerificationReport(String bookUuid) {
            this.bookUuid = bookUuid;
        }

        public void incrementIssues() {
            this.issues++;
        }

        public void addIssue(TemporalIssue issue) {
            this.temporalIssues.add(issue);
        }
    }

    /**
     * 时序问题
     */
    @Data
    public static class TemporalIssue {
        private String entity;
        private int eventChapter;
        private int lastChapter;
        private String issueType;
        private String description;
    }

    /**
     * 因果验证报告
     */
    @Data
    public static class CausalityVerificationReport {
        private String bookUuid;
        private long verificationDuration;
        private List<Map<String, Object>> missingPredecessors = new ArrayList<>();
        private List<Map<String, Object>> weakCausalities = new ArrayList<>();

        public CausalityVerificationReport(String bookUuid) {
            this.bookUuid = bookUuid;
        }

        public void addMissingPredecessor(Map<String, Object> event, String predecessorType) {
            Map<String, Object> missing = new HashMap<>(event);
            missing.put("requiredPredecessor", predecessorType);
            this.missingPredecessors.add(missing);
        }

        public void addWeakCausality(Map<String, Object> event) {
            this.weakCausalities.add(event);
        }
    }
}
