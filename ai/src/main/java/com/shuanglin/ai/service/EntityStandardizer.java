package com.shuanglin.ai.service;

import cn.hutool.core.util.IdUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shuanglin.ai.langchain4j.assistant.DecomposeAssistant;
import com.shuanglin.ai.utils.FileReadUtil;
import com.shuanglin.dao.model.EntityMention;
import com.shuanglin.dao.model.EntityRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 实体标准化服务
 * 用于全局扫描与实体注册表管理（阶段一）
 */
@Slf4j
@Service
public class EntityStandardizer {
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    @Autowired
    private DecomposeAssistant decomposeAssistant;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 扫描并构建实体注册表
     */
    public void scanAndBuildRegistry(String filePath) {
        log.info("【阶段一】开始全局扫描，书籍路径: {}", filePath);

        // 1. 提取全书文本，分块处理（每块10000字）
        List<FileReadUtil.ParseResult> parseResults = FileReadUtil.readEpubFile(new File(filePath));
        // 2. 并行调用LLM提取实体
        List<EntityMention> allMentions = new ArrayList<>();
        for (int i = 1; i < 100; i++) {
            String chunk = parseResults.get(i).getContent();
            log.info("处理第{}/{}块文本...", i , i);
            
            try {
                String llmResult = decomposeAssistant.extractGlobalEntities(chunk);
                List<Map<String, Object>> jsonList = objectMapper.readValue(llmResult, new TypeReference<>() {});
                
                for (Map<String, Object> json : jsonList) {
                    EntityMention mention = EntityMention.fromJson(json);
                    allMentions.add(mention);
                }
            } catch (Exception e) {
                log.error("解析LLM返回结果失败，块索引: {}", i, e);
            }
        }
        
        log.info("共提取{}个实体提及", allMentions.size());
        
        // 3. 别名聚合与实体合并
        Map<String, EntityRegistry> mergedEntities = mergeEntitiesByAlias(allMentions);
        log.info("合并后共{}个唯一实体", mergedEntities.size());
        
        // 4. 保存到MongoDB
        for (EntityRegistry entity : mergedEntities.values()) {
            entity.setBookUuid(IdUtil.getSnowflakeNextIdStr());
            entity.setCreatedAt(new Date());
            mongoTemplate.save(entity, "entity_registry");
        }
        
        log.info("实体注册表构建完成，共{}个实体", mergedEntities.size());
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
     * 别名解析为标准实体ID
     */
    public String resolveAlias(String alias, String bookUuid) {
        Query query = new Query();
        query.addCriteria(Criteria.where("bookUuid").is(bookUuid)
                                  .orOperator(
                                      Criteria.where("standardName").is(alias),
                                      Criteria.where("aliases").in(alias)
                                  ));
        
        EntityRegistry entity = mongoTemplate.findOne(query, EntityRegistry.class, "entity_registry");
        return entity != null ? entity.getEntityId() : null;
    }
    
    /**
     * 根据实体ID查询实体
     */
    public EntityRegistry getEntityById(String entityId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("entityId").is(entityId));
        return mongoTemplate.findOne(query, EntityRegistry.class, "entity_registry");
    }
    
    /**
     * 文本分块
     */
    private List<String> splitIntoChunks(String text, int chunkSize) {
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
    private String generateEntityId(String type, int counter) {
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
}
