package com.shuanglin.dao.neo4j.novel;

import com.shuanglin.dao.neo4j.novel.enums.EntityType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实体节点 - Neo4j实体类
 * 对应systemPrompt-3.0.md第五章节点定义
 * <p>
 * 定义：在故事中持续存在的、可被识别的独立客体，是图中的"名词"
 * Entity是承载状态和传递因果的载体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntityNode {
    
    /**
     * 实体跨越所有章节的恒定身份
     */
    private String uuid;
    
    /**
     * 实体的核心名称
     */
    private String name;
    
    /**
     * 实体分类，对应领域实体子类型
     */
    private EntityType entityType;
    
    /**
     * 实体首次出现的章节索引
     */
    private Integer createdAtChapter;
}
