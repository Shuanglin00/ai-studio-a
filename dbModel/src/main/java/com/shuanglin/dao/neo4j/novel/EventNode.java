package com.shuanglin.dao.neo4j.novel;

import com.shuanglin.dao.neo4j.novel.enums.EventType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 事件节点 - Neo4j实体类
 * 对应systemPrompt-3.0.md第五章节点定义
 * 
 * 定义：在特定章节发生的、实体间交互行为的瞬时记录
 * 事件是系统中状态变化的瞬时驱动者，是图中的"动词"
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventNode {
    
    /**
     * 全局唯一标识符
     */
    private String uuid;
    
    /**
     * 事件发生的章节编号
     */
    private Integer chapterIndex;
    
    /**
     * 事件类型
     */
    private EventType eventType;
    
    /**
     * 对事件的自然语言描述
     */
    private String description;
    
    /**
     * 触发事件的原文片段（推荐）
     */
    private String sourceText;
    
    /**
     * 标记事件是文本明确描述的还是推理得出的（推荐）
     */
    private Boolean isImplicit;
    
    /**
     * 置信度
     */
    private Float confidence;
    
    /**
     * 数据来源
     */
    private String source;
}
