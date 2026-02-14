package com.shuanglin.dao.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 实体注册表MongoDB文档模型
 * 集合名称：entity_registry
 * 用于全书实体的标准化管理
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "entity_registry")
@CompoundIndex(name = "book_name_idx", def = "{'bookUuid': 1, 'standardName': 1}")
public class EntityRegistry {
    /**
     * MongoDB文档ID
     */
    @Id
    private String id;
    
    /**
     * 全局唯一标识符（对应Neo4j中Entity.uuid）
     */
    @Indexed(unique = true)
    private String entityId;
    
    /**
     * 标准名称（首次出场名）
     */
    private String standardName;
    
    /**
     * 所有别名（支持文本检索）
     */
    @TextIndexed
    private List<String> aliases;
    
    /**
     * 实体类型：Character/Location/Organization/Item/Skill
     * 对应 ai 模块的 EntityTypeEnum
     */
    private String entityType;

    /**
     * 实体类型标签（对应 Neo4j 中的 Label）
     * 由 ai 模块的 EntityTypeEnum 提供
     */
    private String entityTypeLabel;
    
    /**
     * 首次出现章节
     */
    private Integer firstMentionChapter;
    
    /**
     * 所属书籍UUID
     */
    @Indexed
    private String bookUuid;
    
    /**
     * 识别置信度（0.0-1.0）
     */
    private Double confidence;
    
    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    /**
     * 实体描述（可选，用于增强生成）
     */
    private String description;

    /**
     * 实体属性（JSON格式，用于存储额外属性）
     */
    private Map<String, Object> properties;

    /**
     * 关联的Neo4j节点UUID
     */
    private String neo4jUuid;

    /**
     * 是否已验证（用于辩证验证阶段）
     */
    private Boolean verified;

    /**
     * 验证时间
     */
    private Date verifiedAt;

    /**
     * 验证备注
     */
    private String verificationNote;

    /**
     * 首次提及的原文片段
     */
    private String firstMentionSource;

    /**
     * 提及次数统计
     */
    private Integer mentionCount;

    /**
     * 提及的章节列表
     */
    private List<Integer> mentionChapters;

    /**
     * 获取所有名称（标准名+别名）
     */
    public List<String> getAllNames() {
        List<String> allNames = new ArrayList<>();
        allNames.add(standardName);
        if (aliases != null) {
            allNames.addAll(aliases);
        }
        return allNames;
    }

    /**
     * 添加别名（去重）
     */
    public void addAlias(String alias) {
        if (aliases == null) {
            aliases = new ArrayList<>();
        }
        if (!aliases.contains(alias) && !alias.equals(standardName)) {
            aliases.add(alias);
        }
    }

    /**
     * 添加章节提及记录
     */
    public void addChapterMention(int chapterIndex) {
        if (mentionChapters == null) {
            mentionChapters = new ArrayList<>();
        }
        if (!mentionChapters.contains(chapterIndex)) {
            mentionChapters.add(chapterIndex);
            mentionCount = mentionChapters.size();
        }
    }
}
