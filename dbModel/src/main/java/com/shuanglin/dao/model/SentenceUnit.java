package com.shuanglin.dao.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 句子单元MongoDB文档模型
 * 集合名称：sentence_units
 * 用于句子级内容分解与建模
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "sentence_units")
@CompoundIndex(name = "book_sequence_idx", def = "{'bookUuid': 1, 'sequenceNumber': 1}")
public class SentenceUnit {
    /**
     * MongoDB文档ID
     */
    @Id
    private String id;
    
    /**
     * 句子唯一ID（如"SENT_001"）
     */
    @Indexed(unique = true)
    private String sentenceId;
    
    /**
     * 所属书籍UUID
     */
    @Indexed
    private String bookUuid;
    
    /**
     * 所属章节索引
     */
    @Indexed
    private Integer chapterIndex;
    
    /**
     * 全局顺序号
     */
    @Indexed
    private Integer sequenceNumber;
    
    /**
     * 句子类型：Dialogue/Action/Description/Thought/StateChange/Narration
     */
    private String sentenceType;
    
    /**
     * 完整原文
     */
    private String originalText;
    
    /**
     * 涉及的实体ID列表
     */
    private List<String> entities;
    
    /**
     * 结构化数据（根据句子类型不同，内容不同）
     */
    private Map<String, Object> structuredData;
    
    /**
     * 创建时间
     */
    private Date createdAt;
    
    /**
     * 添加实体引用
     */
    public void addEntity(String entityId) {
        if (entities == null) {
            entities = new ArrayList<>();
        }
        if (!entities.contains(entityId)) {
            entities.add(entityId);
        }
    }
    
    /**
     * 设置结构化数据字段
     */
    public void setStructuredDataField(String key, Object value) {
        if (structuredData == null) {
            structuredData = new HashMap<>();
        }
        structuredData.put(key, value);
    }
    
    /**
     * 获取结构化数据字段
     */
    public Object getStructuredDataField(String key) {
        if (structuredData == null) {
            return null;
        }
        return structuredData.get(key);
    }
}
