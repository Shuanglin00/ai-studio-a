package com.shuanglin.ai.novel.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shuanglin.ai.langchain4j.assistant.DecomposeAssistant;
import com.shuanglin.dao.model.EntityRegistry;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 实体验证服务
 * 负责阶段三的实体辩证验证与修正
 */
@Slf4j
@Service
public class EntityVerificationService {

    @Autowired
    private DecomposeAssistant decomposeAssistant;

    @Autowired
    private NovelEntityService novelEntityService;

    @Autowired
    private MongoTemplate mongoTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 验证实体一致性（自动）
     * @param bookUuid 书籍UUID
     * @return 验证报告
     */
    public VerificationReport verifyEntityConsistency(String bookUuid) {
        log.info("【阶段三】开始验证实体一致性，书籍UUID: {}", bookUuid);

        long startTime = System.currentTimeMillis();
        VerificationReport report = new VerificationReport(bookUuid);

        // 1. 获取所有实体
        List<EntityRegistry> entities = novelEntityService.getAllEntities(bookUuid);
        report.setTotalEntities(entities.size());

        // 2. 转换为JSON
        String entityListJson = convertEntitiesToJson(entities);

        // 3. 获取上下文文本
        String contextText = fetchContextText(bookUuid, 5000);

        // 4. 调用LLM验证
        try {
            String verificationResult = decomposeAssistant.verifyEntities(entityListJson, contextText);
            List<Map<String, Object>> results = objectMapper.readValue(
                    verificationResult,
                    new TypeReference<List<Map<String, Object>>>() {}
            );

            // 5. 处理验证结果
            for (Map<String, Object> result : results) {
                String entityId = (String) result.get("entityId");
                boolean isValid = (Boolean) result.getOrDefault("isValid", true);

                if (!isValid) {
                    report.incrementIssues();
                    List<String> issues = (List<String>) result.getOrDefault("issues", Collections.emptyList());

                    // 处理修正建议
                    Map<String, Object> correction = (Map<String, Object>) result.get("suggestedCorrection");
                    if (correction != null && !correction.isEmpty()) {
                        EntityRegistry entity = entities.stream()
                                .filter(e -> e.getEntityId().equals(entityId))
                                .findFirst()
                                .orElse(null);

                        if (entity != null) {
                            Correction c = applyCorrection(entity, correction);
                            report.addCorrection(c);
                        }
                    }
                } else {
                    report.incrementValidEntities();
                }
            }

        } catch (Exception e) {
            log.error("实体验证失败", e);
            report.setErrorMessage(e.getMessage());
        }

        long endTime = System.currentTimeMillis();
        report.setVerificationDuration(endTime - startTime);

        log.info("实体验证完成，有效: {}, 有问题: {}, 耗时: {}ms",
                report.getValidEntities(),
                report.getIssues(),
                report.getVerificationDuration());

        return report;
    }

    /**
     * 跨章节实体追踪与消歧
     * @param bookUuid 书籍UUID
     * @return 追踪报告
     */
    public EntityTrackingReport trackEntitiesAcrossChapters(String bookUuid) {
        log.info("【阶段三】开始跨章节实体追踪，书籍UUID: {}", bookUuid);

        long startTime = System.currentTimeMillis();
        EntityTrackingReport report = new EntityTrackingReport(bookUuid);

        // 获取所有实体
        List<EntityRegistry> entities = novelEntityService.getAllEntities(bookUuid);

        // 读取章节文本
        // 这里假设有一个方法来获取所有章节的文本
        // 简化处理：追踪主要角色
        List<EntityRegistry> characters = entities.stream()
                .filter(e -> "Character".equals(e.getEntityType()))
                .collect(Collectors.toList());

        for (EntityRegistry character : characters) {
            try {
                String trackingResult = trackSingleEntity(bookUuid, character);
                Map<String, Object> tracking = objectMapper.readValue(
                        trackingResult,
                        new TypeReference<Map<String, Object>>() {}
                );

                boolean isConsistent = (Boolean) tracking.getOrDefault("isConsistent", true);
                if (!isConsistent) {
                    report.incrementInconsistentEntities();
                } else {
                    report.incrementConsistentEntities();
                }

                List<Map<String, Object>> possibleEntities = (List<Map<String, Object>>) tracking.get("possibleEntities");
                if (possibleEntities != null) {
                    report.addEntityTrackingResult(character.getStandardName(), tracking);
                }

            } catch (Exception e) {
                log.error("追踪实体失败: {}", character.getStandardName(), e);
            }
        }

        long endTime = System.currentTimeMillis();
        report.setTrackingDuration(endTime - startTime);

        log.info("实体追踪完成，一致: {}, 不一致: {}, 耗时: {}ms",
                report.getConsistentEntities(),
                report.getInconsistentEntities(),
                report.getTrackingDuration());

        return report;
    }

    /**
     * 自动修正实体（无需人工确认）
     * @param report 验证报告
     * @return 修正结果
     */
    public AutoCorrectResult autoCorrect(VerificationReport report) {
        log.info("【阶段三】开始自动修正实体");

        AutoCorrectResult result = new AutoCorrectResult(report.getBookUuid());
        result.setTotalCorrections(report.getCorrections().size());

        for (Correction correction : report.getCorrections()) {
            try {
                // 直接应用修正
                applyDirectCorrection(correction);
                result.incrementSuccessCount();
                log.info("修正成功: {} -> {}",
                        correction.getOriginalName(),
                        correction.getNewName());
            } catch (Exception e) {
                result.incrementFailedCount();
                log.error("修正失败: {}", correction.getOriginalName(), e);
            }
        }

        log.info("自动修正完成，成功: {}, 失败: {}",
                result.getSuccessCount(),
                result.getFailedCount());

        return result;
    }

    /**
     * 追踪单个实体
     */
    private String trackSingleEntity(String bookUuid, EntityRegistry entity) throws Exception {
        // 收集实体在不同章节的提及
        List<Map<String, Object>> chapterTextPairs = new ArrayList<>();

        // 这里简化处理，实际应该读取章节文件
        String entityName = entity.getStandardName();
        String chapterTextPairsJson = objectMapper.writeValueAsString(chapterTextPairs);

        return decomposeAssistant.trackEntityAcrossChapters(entityName, chapterTextPairsJson);
    }

    /**
     * 获取上下文文本
     */
    private String fetchContextText(String bookUuid, int maxLength) {
        // 简化处理，实际应该从MongoDB获取章节内容
        return "";
    }

    /**
     * 转换实体列表为JSON
     */
    private String convertEntitiesToJson(List<EntityRegistry> entities) {
        List<Map<String, Object>> entityList = entities.stream()
                .map(entity -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("entityId", entity.getEntityId());
                    map.put("standardName", entity.getStandardName());
                    map.put("aliases", entity.getAliases());
                    map.put("entityType", entity.getEntityType());
                    map.put("confidence", entity.getConfidence());
                    return map;
                })
                .collect(Collectors.toList());

        try {
            return objectMapper.writeValueAsString(entityList);
        } catch (Exception e) {
            log.error("序列化实体列表失败", e);
            return "[]";
        }
    }

    /**
     * 应用修正
     */
    private Correction applyCorrection(EntityRegistry entity, Map<String, Object> correction) {
        Correction c = new Correction();
        c.setEntityId(entity.getEntityId());
        c.setOriginalName(entity.getStandardName());

        String newName = (String) correction.get("newStandardName");
        if (newName != null) {
            entity.setStandardName(newName);
            c.setNewName(newName);
        }

        List<String> newAliases = (List<String>) correction.get("newAliases");
        if (newAliases != null) {
            entity.setAliases(newAliases);
            c.setNewAliases(newAliases);
        }

        String newType = (String) correction.get("newEntityType");
        if (newType != null) {
            entity.setEntityType(newType);
            c.setNewType(newType);
        }

        entity.setUpdatedAt(new Date());
        mongoTemplate.save(entity, "entity_registry");

        return c;
    }

    /**
     * 直接应用修正（无需人工确认）
     */
    private void applyDirectCorrection(Correction correction) {
        Query query = new Query();
        query.addCriteria(Criteria.where("entityId").is(correction.getEntityId()));

        EntityRegistry entity = mongoTemplate.findOne(query, EntityRegistry.class, "entity_registry");
        if (entity != null) {
            if (correction.getNewName() != null) {
                entity.setStandardName(correction.getNewName());
            }
            if (correction.getNewAliases() != null) {
                entity.setAliases(correction.getNewAliases());
            }
            if (correction.getNewType() != null) {
                entity.setEntityType(correction.getNewType());
            }

            entity.setVerified(true);
            entity.setVerifiedAt(new Date());
            entity.setVerificationNote(correction.getNotes());
            entity.setUpdatedAt(new Date());

            mongoTemplate.save(entity, "entity_registry");
        }
    }

    /**
     * 验证报告
     */
    @Data
    public static class VerificationReport {
        private String bookUuid;
        private int totalEntities;
        private int validEntities;
        private int issues;
        private long verificationDuration;
        private String errorMessage;
        private List<Correction> corrections = new ArrayList<>();

        public VerificationReport(String bookUuid) {
            this.bookUuid = bookUuid;
        }

        public void incrementValidEntities() {
            this.validEntities++;
        }

        public void incrementIssues() {
            this.issues++;
        }

        public void addCorrection(Correction correction) {
            this.corrections.add(correction);
        }
    }

    /**
     * 实体追踪报告
     */
    @Data
    public static class EntityTrackingReport {
        private String bookUuid;
        private int consistentEntities;
        private int inconsistentEntities;
        private long trackingDuration;
        private Map<String, Map<String, Object>> trackingResults = new HashMap<>();

        public EntityTrackingReport(String bookUuid) {
            this.bookUuid = bookUuid;
        }

        public void incrementConsistentEntities() {
            this.consistentEntities++;
        }

        public void incrementInconsistentEntities() {
            this.inconsistentEntities++;
        }

        public void addEntityTrackingResult(String entityName, Map<String, Object> result) {
            this.trackingResults.put(entityName, result);
        }
    }

    /**
     * 修正记录
     */
    @Data
    public static class Correction {
        private String entityId;
        private String originalName;
        private String newName;
        private List<String> newAliases;
        private String newType;
        private String notes;
    }

    /**
     * 自动修正结果
     */
    @Data
    public static class AutoCorrectResult {
        private String bookUuid;
        private int totalCorrections;
        private int successCount;
        private int failedCount;

        public AutoCorrectResult(String bookUuid) {
            this.bookUuid = bookUuid;
        }

        public void incrementSuccessCount() {
            this.successCount++;
        }

        public void incrementFailedCount() {
            this.failedCount++;
        }
    }
}
