package com.shuanglin.dao.neo4j.novel.enums;

import lombok.Getter;

/**
 * Neo4j关系类型枚举
 * 对应systemPrompt-3.0.md第5.3节关系类型定义
 */
@Getter
public enum RelationshipTypeEnum {
    
    // ==================== 结构与时序关系 ====================
    
    /**
     * 章节与其包含的所有新内容的关联
     * (Chapter)-[:HAS_CONTENT]->(Event|State|Entity)
     */
    HAS_CONTENT("HAS_CONTENT", "包含内容", "结构关系"),
    
    /**
     * 实体指向其一个状态快照
     * (Entity)-[:HAS_STATE]->(State)
     */
    HAS_STATE("HAS_STATE", "拥有状态", "结构关系"),
    
    /**
     * 核心时序关系，连接状态版本链，指向紧邻的后继状态
     * (State)-[:NEXT_STATE]->(State)
     */
    NEXT_STATE("NEXT_STATE", "下一状态", "时序关系"),
    
    // ==================== 叙事与语义关系 ====================
    
    /**
     * 连接实体及其参与的事件
     * (Entity)-[:PARTICIPATES_IN {role: String}]->(Event)
     */
    PARTICIPATES_IN("PARTICIPATES_IN", "参与", "叙事关系"),
    
    /**
     * 描述事件发生的地点实体
     * (Event)-[:OCCURRED_IN]->(Location)
     */
    OCCURRED_IN("OCCURRED_IN", "发生于", "叙事关系"),
    
    /**
     * 连接事件与其直接的前置因果事件
     * (Event)-[:TRIGGERED_BY]->(Event)
     */
    TRIGGERED_BY("TRIGGERED_BY", "被触发", "因果关系"),
    
    /**
     * 实体消歧关系，表示两个实体节点可能指代同一现实客体
     * (Entity)-[:POSSIBLY_IDENTICAL]->(Entity)
     */
    POSSIBLY_IDENTICAL("POSSIBLY_IDENTICAL", "可能相同", "语义关系"),
    
    /**
     * 连接摘要及其描述的核心实体
     * (Summary)-[:SUMMARIZES_ENTITY]->(Entity)
     */
    SUMMARIZES_ENTITY("SUMMARIZES_ENTITY", "总结实体", "语义关系"),
    
    /**
     * 连接摘要及其涵盖的章节范围
     * (Summary)-[:COVERS_CHAPTERS]->(Chapter)
     */
    COVERS_CHAPTERS("COVERS_CHAPTERS", "涵盖章节", "语义关系"),
    
    // ==================== 精准还原关系 ====================
    
    /**
     * 顺序关系，连接所有句子级节点，形成线性链
     * (SentenceNode)-[:NEXT_SENTENCE {sequenceNumber: Integer}]->(SentenceNode)
     */
    NEXT_SENTENCE("NEXT_SENTENCE", "下一句", "时序关系"),
    
    /**
     * 说话关系，连接实体与其说出的对话
     * (Entity)-[:SPEAKS {sequenceNumber: Integer}]->(DialogueNode)
     */
    SPEAKS("SPEAKS", "说话", "叙事关系"),
    
    /**
     * 执行动作，连接实体与其执行的动作事件
     * (Entity)-[:PERFORMS {sequenceNumber: Integer}]->(ActionEvent)
     */
    PERFORMS("PERFORMS", "执行", "叙事关系"),
    
    /**
     * 动作客体，连接动作事件与动作指向的实体
     * (ActionEvent)-[:TARGETS]->(Entity)
     */
    TARGETS("TARGETS", "指向", "叙事关系"),
    
    /**
     * 描写关系，连接描写节点与被描写的实体
     * (DescriptionNode)-[:DESCRIBES]->(Entity)
     */
    DESCRIBES("DESCRIBES", "描述", "语义关系"),
    
    /**
     * 心理活动，连接实体与其心理活动
     * (Entity)-[:THINKS {sequenceNumber: Integer}]->(ThoughtNode)
     */
    THINKS("THINKS", "思考", "叙事关系"),
    
    /**
     * 叙述说明，连接章节与叙述节点
     * (Chapter)-[:NARRATES {sequenceNumber: Integer}]->(NarrationNode)
     */
    NARRATES("NARRATES", "叙述", "结构关系");
    
    /**
     * 关系类型名称（Neo4j中使用）
     */
    private final String type;
    
    /**
     * 中文描述
     */
    private final String zhName;
    
    /**
     * 关系分类
     */
    private final String category;
    
    RelationshipTypeEnum(String type, String zhName, String category) {
        this.type = type;
        this.zhName = zhName;
        this.category = category;
    }
    
    /**
     * 根据类型名称获取枚举
     */
    public static RelationshipTypeEnum fromType(String type) {
        for (RelationshipTypeEnum rel : values()) {
            if (rel.type.equals(type)) {
                return rel;
            }
        }
        throw new IllegalArgumentException("未知的关系类型: " + type);
    }
}
