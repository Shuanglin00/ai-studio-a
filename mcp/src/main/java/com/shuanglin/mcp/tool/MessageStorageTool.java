package com.shuanglin.mcp.tool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shuanglin.dao.classification.MessageCategoryDefinition;
import com.shuanglin.dao.classification.MessageClassification;
import com.shuanglin.dao.classification.MessageCollectionBatch;
import com.shuanglin.dao.classification.enums.ClassificationSource;
import com.shuanglin.dao.classification.enums.MessageCategory;
import com.shuanglin.dao.classification.repository.MessageCategoryDefinitionRepository;
import com.shuanglin.dao.classification.repository.MessageClassificationRepository;
import com.shuanglin.dao.classification.repository.MessageCollectionBatchRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 消息存储工具
 * 提供消息批次的存储、查询和分类结果管理功能
 */
@Component
public class MessageStorageTool {

    private final MessageCollectionBatchRepository batchRepository;
    private final MessageClassificationRepository classificationRepository;
    private final MessageCategoryDefinitionRepository categoryDefinitionRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public MessageStorageTool(
            MessageCollectionBatchRepository batchRepository,
            MessageClassificationRepository classificationRepository,
            MessageCategoryDefinitionRepository categoryDefinitionRepository) {
        this.batchRepository = batchRepository;
        this.classificationRepository = classificationRepository;
        this.categoryDefinitionRepository = categoryDefinitionRepository;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 构造函数用于测试
     */
    public MessageStorageTool(
            MessageCollectionBatchRepository batchRepository,
            MessageClassificationRepository classificationRepository,
            ObjectMapper objectMapper) {
        this.batchRepository = batchRepository;
        this.classificationRepository = classificationRepository;
        this.categoryDefinitionRepository = null;
        this.objectMapper = objectMapper;
    }

    /**
     * 保存消息批次
     *
     * @param batchJson 批次数据JSON
     * @return 保存的批次ID
     */
    @Tool(name = "saveMessageBatch",
            description = "保存消息收集批次到数据库，返回批次ID")
    public String saveMessageBatch(
            @ToolParam(description = "批次数据JSON字符串") String batchJson) {
        try {
            MessageCollectionBatch batch = parseBatch(batchJson);
            validateBatch(batch);

            if (batchRepository != null) {
                MessageCollectionBatch saved = batchRepository.save(batch);
                return saved.getId();
            } else {
                return batch.getId();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to save batch: " + e.getMessage(), e);
        }
    }

    /**
     * 根据ID获取批次
     *
     * @param batchId 批次ID
     * @return 批次JSON字符串
     */
    @Tool(name = "getBatchById",
            description = "根据ID获取消息批次")
    public String getBatchById(
            @ToolParam(description = "批次ID") String batchId) {
        if (batchRepository == null) {
            return "{\"error\": \"Repository not available\"}";
        }

        Optional<MessageCollectionBatch> batch = batchRepository.findById(batchId);
        if (batch.isPresent()) {
            try {
                return objectMapper.writeValueAsString(batch.get());
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to serialize batch", e);
            }
        }
        return "{\"error\": \"Batch not found\"}";
    }

    /**
     * 根据群号获取批次列表
     *
     * @param groupId 群号
     * @return 批次列表JSON
     */
    @Tool(name = "getBatchesByGroup",
            description = "根据群号获取所有消息批次")
    public String getBatchesByGroup(
            @ToolParam(description = "群号") String groupId) {
        if (batchRepository == null) {
            return "[]";
        }

        try {
            var batches = batchRepository.findByGroupId(groupId);
            return objectMapper.writeValueAsString(batches);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize batches", e);
        }
    }

    /**
     * 更新消息分类（用于手动标注）
     *
     * @param batchId      批次ID
     * @param categoryCode 分类编码
     * @param userId       用户ID（手动标注时）
     * @return 是否成功
     */
    @Tool(name = "updateClassification",
            description = "更新消息分类（用于手动标注）")
    public boolean updateClassification(
            @ToolParam(description = "批次ID") String batchId,
            @ToolParam(description = "分类编码") String categoryCode,
            @ToolParam(description = "用户ID（手动标注时）") Long userId) {
        try {
            if (classificationRepository == null || batchRepository == null) {
                return false;
            }

            MessageCategory category = MessageCategory.fromCode(categoryCode);

            // 获取批次信息
            Optional<MessageCollectionBatch> batchOpt = batchRepository.findById(batchId);
            if (batchOpt.isEmpty()) {
                return false;
            }
            MessageCollectionBatch batch = batchOpt.get();

            // 创建或更新分类记录
            MessageClassification classification = classificationRepository.findByBatchId(batchId)
                    .orElse(new MessageClassification());

            classification.setBatchId(batchId);
            classification.setGroupId(batch.getGroupId());
            classification.setTriggerMessageId(batch.getTriggerMessageId());
            classification.setCategory(category);
            classification.setSource(userId != null ? ClassificationSource.MANUAL : ClassificationSource.AUTO);
            classification.setClassifiedBy(userId);
            classification.setClassifiedAt(LocalDateTime.now());
            classification.setConfidence(1.0); // 手动标注置信度为1.0
            classification.setReasoning(userId != null ? "手动标注" : "自动分类");

            classificationRepository.save(classification);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取分类结果
     *
     * @param batchId 批次ID
     * @return 分类结果JSON
     */
    @Tool(name = "getClassificationResult",
            description = "获取批次的分类结果")
    public String getClassificationResult(
            @ToolParam(description = "批次ID") String batchId) {
        if (classificationRepository == null) {
            return "{\"error\": \"Repository not available\"}";
        }

        Optional<MessageClassification> classification = classificationRepository.findByBatchId(batchId);
        if (classification.isPresent()) {
            try {
                return objectMapper.writeValueAsString(classification.get());
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to serialize classification", e);
            }
        }
        return "{\"error\": \"Classification not found\"}";
    }

    /**
     * 获取所有分类标签定义
     *
     * @return 分类定义列表JSON
     */
    @Tool(name = "getCategoryDefinitions",
            description = "获取所有启用的分类标签定义")
    public String getCategoryDefinitions() {
        if (categoryDefinitionRepository == null) {
            // 返回默认分类
            return getDefaultCategories();
        }

        try {
            var categories = categoryDefinitionRepository.findByEnabledTrueOrderByPriorityAsc();
            return objectMapper.writeValueAsString(categories);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize categories", e);
        }
    }

    /**
     * 解析批次JSON
     */
    public MessageCollectionBatch parseBatch(String batchJson) throws JsonProcessingException {
        return objectMapper.readValue(batchJson, MessageCollectionBatch.class);
    }

    /**
     * 验证批次数据完整性
     */
    public void validateBatch(MessageCollectionBatch batch) {
        if (batch == null) {
            throw new IllegalArgumentException("Batch cannot be null");
        }
        if (batch.getId() == null || batch.getId().isBlank()) {
            throw new IllegalArgumentException("Batch ID is required");
        }
        if (batch.getGroupId() == null || batch.getGroupId().isBlank()) {
            throw new IllegalArgumentException("Group ID is required");
        }
    }

    /**
     * 提取批次ID
     */
    public String extractBatchId(String batchJson) throws JsonProcessingException {
        MessageCollectionBatch batch = parseBatch(batchJson);
        return batch.getId();
    }

    /**
     * 提取群号
     */
    public String extractGroupId(String batchJson) throws JsonProcessingException {
        MessageCollectionBatch batch = parseBatch(batchJson);
        return batch.getGroupId();
    }

    /**
     * 统计消息数量
     */
    public int countMessages(String batchJson) throws JsonProcessingException {
        MessageCollectionBatch batch = parseBatch(batchJson);
        if (batch.getMessages() == null) {
            return 0;
        }
        return batch.getMessages().size();
    }

    /**
     * 获取默认分类
     */
    private String getDefaultCategories() {
        return """
                [
                    {"code":"meme","name":"弔图","description":"搞笑图片、表情包"},
                    {"code":"hell-joke","name":"地狱笑话","description":"黑色幽默"},
                    {"code":"regional-black","name":"地域黑","description":"地域歧视言论"},
                    {"code":"normal","name":"正常","description":"普通聊天内容"},
                    {"code":"spam","name":"刷屏","description":"无意义重复内容"}
                ]
                """;
    }
}
