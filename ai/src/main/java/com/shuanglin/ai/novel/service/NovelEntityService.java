package com.shuanglin.ai.novel.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shuanglin.ai.langchain4j.assistant.DecomposeAssistant;
import com.shuanglin.ai.novel.enums.EntityTypeEnum;
import com.shuanglin.ai.utils.FileReadUtil;
import com.shuanglin.dao.model.EntityMention;
import com.shuanglin.dao.model.EntityRegistry;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 小说实体扫描服务
 * 负责全文扫描实体（阶段一）
 */
@Slf4j
@Service
public class NovelEntityService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private DecomposeAssistant decomposeAssistant;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 扫描整本小说提取实体
     * @param epubPath EPUB文件路径
     * @param bookUuid 书籍UUID
     * @param bookName 书籍名称
     * @return 实体扫描报告
     */
    public EntityScanReport scanFullNovel(String epubPath, String bookUuid, String bookName) {
        log.info("【阶段一】开始全文扫描实体，书籍: {}, 路径: {}", bookName, epubPath);

        long startTime = System.currentTimeMillis();
        EntityScanReport report = new EntityScanReport(bookUuid, bookName);

        // 1. 读取EPUB文件
        List<FileReadUtil.ParseResult> parseResults = FileReadUtil.readEpubFile(new File(epubPath));
        report.setTotalChapters(parseResults.size());

        // 2. 遍历全书提取实体
        List<EntityMention> allMentions = new ArrayList<>();
        int chunkSize = 8000; // 每块8000字

        for (int chapterIdx = 0; chapterIdx < parseResults.size(); chapterIdx++) {
            FileReadUtil.ParseResult chapter = parseResults.get(chapterIdx);
            String chapterText = chapter.getContent();
            int chapterIndex = chapterIdx + 1;

            // 分块处理长章节
            List<String> chunks = splitIntoChunks(chapterText, chunkSize);
            for (int chunkIdx = 0; chunkIdx < chunks.size(); chunkIdx++) {
                String chunk = chunks.get(chunkIdx);
                log.debug("处理第{}章第{}/{}块...", chapterIndex, chunkIdx + 1, chunks.size());

                try {
                    // 添加章节上下文信息
                    String enhancedChunk = String.format(
                            "【第%d章 %s】\n%s",
                            chapterIndex,
                            chapter.getTitle(),
                            chunk
                    );

                    String llmResult = decomposeAssistant.extractGlobalEntities(enhancedChunk);
                    // 去除 markdown 代码块 (处理 ```json ... ``` 和 ``` ... ``` 两种格式)
                    String jsonStr = llmResult.trim();
                    if (jsonStr.startsWith("```")) {
                        // 找到第一个换行后的内容
                        int firstNewline = jsonStr.indexOf('\n');
                        int lastBackticks = jsonStr.lastIndexOf("```");
                        if (firstNewline > 0 && lastBackticks > firstNewline) {
                            jsonStr = jsonStr.substring(firstNewline + 1, lastBackticks).trim();
                        } else if (lastBackticks > 3) {
                            jsonStr = jsonStr.substring(3, lastBackticks).trim();
                        }
                    }
                    List<Map<String, Object>> jsonList = objectMapper.readValue(
                            jsonStr,
                            new TypeReference<List<Map<String, Object>>>() {}
                    );

                    for (Map<String, Object> json : jsonList) {
                        EntityMention mention = EntityMention.fromJson(json);
                        mention.setFirstMention(String.format("第%d章: %s", chapterIndex, mention.getFirstMention()));
                        allMentions.add(mention);
                    }

                    report.incrementProcessedChunks();

                } catch (Exception e) {
                    log.error("解析LLM返回结果失败，章节: {}, 块: {}", chapterIndex, chunkIdx, e);
                    report.incrementFailedChunks();
                }
            }
        }

        log.info("共提取{}个实体提及", allMentions.size());
        report.setTotalMentions(allMentions.size());

        // 3. 别名聚合与实体合并
        Map<String, EntityRegistry> mergedEntities = mergeEntitiesByAlias(allMentions);
        log.info("合并后共{}个唯一实体", mergedEntities.size());
        report.setUniqueEntities(mergedEntities.size());

        // 4. 保存到MongoDB
        List<EntityRegistry> savedEntities = new ArrayList<>();
        for (EntityRegistry entity : mergedEntities.values()) {
            entity.setBookUuid(bookUuid);
            entity.setCreatedAt(new Date());
            entity.setMentionCount(0);
            entity.setMentionChapters(new ArrayList<>());

            // 检查是否已存在
            Query query = new Query();
            query.addCriteria(Criteria.where("bookUuid").is(bookUuid)
                    .and("standardName").is(entity.getStandardName()));
            EntityRegistry existing = mongoTemplate.findOne(query, EntityRegistry.class, "entity_registry");

            if (existing != null) {
                // 更新已有实体
                existing.addAlias(entity.getStandardName());
                entity.getAliases().forEach(existing::addAlias);
                entity.setUpdatedAt(new Date());
                mongoTemplate.save(existing, "entity_registry");
                savedEntities.add(existing);
            } else {
                mongoTemplate.save(entity, "entity_registry");
                savedEntities.add(entity);
            }
        }

        // 5. 更新实体统计信息
        updateEntityStatistics(bookUuid);

        long endTime = System.currentTimeMillis();
        report.setScanDuration(endTime - startTime);
        report.setSavedEntities(savedEntities.size());

        log.info("实体扫描完成，共{}个实体，耗时: {}ms", savedEntities.size(), report.getScanDuration());

        return report;
    }

    /**
     * 基于RAG检索相关实体（用于事件识别阶段）
     * @param bookUuid 书籍UUID
     * @param query 查询文本
     * @param limit 返回数量限制
     * @return 相关实体列表
     */
    public List<EntityRegistry> retrieveEntities(String bookUuid, String query, int limit) {
        // 1. 使用标准名称和别名进行模糊匹配
        Query entityQuery = new Query();
        entityQuery.addCriteria(Criteria.where("bookUuid").is(bookUuid));
        entityQuery.limit(limit);

        List<EntityRegistry> allEntities = mongoTemplate.find(entityQuery, EntityRegistry.class, "entity_registry");

        // 2. 计算相关性分数（简单实现：名称包含查询词或查询词包含名称）
        return allEntities.stream()
                .filter(entity -> {
                    String lowerQuery = query.toLowerCase();
                    boolean match = entity.getStandardName().toLowerCase().contains(lowerQuery);
                    if (!match && entity.getAliases() != null) {
                        match = entity.getAliases().stream()
                                .anyMatch(alias -> alias.toLowerCase().contains(lowerQuery));
                    }
                    return match;
                })
                .sorted((e1, e2) -> {
                    int c1 = e1.getMentionCount() != null ? e1.getMentionCount() : 0;
                    int c2 = e2.getMentionCount() != null ? e2.getMentionCount() : 0;
                    return Integer.compare(c2, c1); // 按提及次数降序
                })
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 获取书籍所有实体
     * @param bookUuid 书籍UUID
     * @return 实体列表
     */
    public List<EntityRegistry> getAllEntities(String bookUuid) {
        Query query = new Query();
        query.addCriteria(Criteria.where("bookUuid").is(bookUuid));
        return mongoTemplate.find(query, EntityRegistry.class, "entity_registry");
    }

    /**
     * 按类型获取实体
     * @param bookUuid 书籍UUID
     * @param entityType 实体类型
     * @return 实体列表
     */
    public List<EntityRegistry> getEntitiesByType(String bookUuid, String entityType) {
        Query query = new Query();
        query.addCriteria(Criteria.where("bookUuid").is(bookUuid)
                .and("entityType").is(entityType));
        return mongoTemplate.find(query, EntityRegistry.class, "entity_registry");
    }

    /**
     * 获取实体注册表JSON（用于LLM调用）
     * @param bookUuid 书籍UUID
     * @return JSON字符串
     */
    public String getEntityRegistryJson(String bookUuid) {
        List<EntityRegistry> entities = getAllEntities(bookUuid);

        List<Map<String, Object>> registryList = entities.stream()
                .map(entity -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("entityId", entity.getEntityId());
                    map.put("standardName", entity.getStandardName());
                    map.put("aliases", entity.getAliases());
                    map.put("entityType", entity.getEntityType());
                    map.put("firstMentionChapter", entity.getFirstMentionChapter());
                    return map;
                })
                .collect(Collectors.toList());

        try {
            return objectMapper.writeValueAsString(registryList);
        } catch (Exception e) {
            log.error("序列化实体注册表失败", e);
            return "[]";
        }
    }

    /**
     * 别名聚合逻辑
     */
    private Map<String, EntityRegistry> mergeEntitiesByAlias(List<EntityMention> mentions) {
        Map<String, EntityRegistry> result = new HashMap<>();
        Map<String, String> aliasToEntityId = new HashMap<>();
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
                entity.setFirstMentionSource(mention.getFirstMention());
                entity.setFirstMentionChapter(1); // 将在后续更新
                result.put(entityId, entity);
            } else {
                // 合并别名
                EntityRegistry existing = result.get(entityId);
                if (mention.getAliases() != null) {
                    for (String alias : mention.getAliases()) {
                        existing.addAlias(alias);
                    }
                }
            }

            // 更新别名映射
            for (String name : mention.getAllNames()) {
                aliasToEntityId.put(name, entityId);
            }
        }

        return result;
    }

    /**
     * 更新实体统计信息
     */
    private void updateEntityStatistics(String bookUuid) {
        // 可以在这里添加更多统计逻辑
        log.debug("实体统计信息已更新");
    }

    /**
     * 文本分块
     */
    private List<String> splitIntoChunks(String text, int chunkSize) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return chunks;
        }

        int length = text.length();
        for (int i = 0; i < length; i += chunkSize) {
            int end = Math.min(i + chunkSize, length);
            chunks.add(text.substring(i, end));
        }

        return chunks;
    }

    /**
     * 生成实体ID
     * 使用 EntityTypeEnum 的 label 作为前缀
     */
    private String generateEntityId(String type, int counter) {
        // 尝试从枚举获取label
        EntityTypeEnum enumType = EntityTypeEnum.fromLabel(type);
        if (enumType != null) {
            return String.format("%s_%03d", enumType.getLabel().toUpperCase(), counter);
        }

        // 兼容旧类型
        String prefix = switch (type) {
            case "Character" -> "CHARACTER";
            case "Location" -> "LOCATION";
            case "Organization" -> "ORGANIZATION";
            case "Item" -> "ITEM";
            case "Skill" -> "SKILL";
            default -> "ENTITY";
        };
        return String.format("%s_%03d", prefix, counter);
    }

    /**
     * 将实体类型字符串转换为枚举并设置标签
     */
    private void setEntityTypeWithLabel(EntityRegistry entity, String type) {
        entity.setEntityType(type);
        EntityTypeEnum enumType = EntityTypeEnum.fromLabel(type);
        if (enumType != null) {
            entity.setEntityTypeLabel(enumType.getLabel());
        }
    }

    /**
     * 实体扫描报告
     */
    @Data
    public static class EntityScanReport {
        private String bookUuid;
        private String bookName;
        private int totalChapters;
        private int totalMentions;
        private int uniqueEntities;
        private int savedEntities;
        private int processedChunks;
        private int failedChunks;
        private long scanDuration;

        public EntityScanReport(String bookUuid, String bookName) {
            this.bookUuid = bookUuid;
            this.bookName = bookName;
        }

        public void incrementProcessedChunks() {
            this.processedChunks++;
        }

        public void incrementFailedChunks() {
            this.failedChunks++;
        }
    }
}
