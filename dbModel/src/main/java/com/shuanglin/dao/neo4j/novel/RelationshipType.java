package com.shuanglin.dao.neo4j.novel;

/**
 * Neo4j关系类型常量定义
 * 对应systemPrompt-3.0.md第5.3节关系类型定义
 */
public class RelationshipType {
    
    // ==================== 结构与时序关系 ====================
    
    /**
     * 章节与其包含的所有新内容的关联
     * (Chapter)-[:HAS_CONTENT]->(Event|State|Entity)
     */
    public static final String HAS_CONTENT = "HAS_CONTENT";
    
    /**
     * 实体指向其一个状态快照
     * (Entity)-[:HAS_STATE]->(State)
     */
    public static final String HAS_STATE = "HAS_STATE";
    
    /**
     * 核心时序关系，连接状态版本链，指向紧邻的后继状态
     * (State {chapterIndex: N})-[:NEXT_STATE]->(State {chapterIndex: M}) where M > N
     */
    public static final String NEXT_STATE = "NEXT_STATE";
    
    // ==================== 叙事与语义关系 ====================
    
    /**
     * 连接实体及其参与的事件，role属性描述其角色（如 '攻击方', '对话发起者', '被救者'）
     * (Entity)-[:PARTICIPATES_IN {role: String}]->(Event)
     */
    public static final String PARTICIPATES_IN = "PARTICIPATES_IN";
    
    /**
     * 描述事件发生的地点实体
     * (Event)-[:OCCURRED_IN]->(Location)
     */
    public static final String OCCURRED_IN = "OCCURRED_IN";
    
    /**
     * 连接事件与其直接的前置因果事件
     * (Event)-[:TRIGGERED_BY]->(Event)
     */
    public static final String TRIGGERED_BY = "TRIGGERED_BY";
    
    /**
     * 实体消歧关系，表示两个实体节点可能指代同一现实客体（如"炎帝"和"萧炎"）
     * (Entity)-[:POSSIBLY_IDENTICAL]->(Entity)
     */
    public static final String POSSIBLY_IDENTICAL = "POSSIBLY_IDENTICAL";
    
    /**
     * 连接摘要及其描述的核心实体
     * (Summary)-[:SUMMARIZES_ENTITY]->(Entity)
     */
    public static final String SUMMARIZES_ENTITY = "SUMMARIZES_ENTITY";
    
    /**
     * 连接摘要及其涵盖的章节范围
     * (Summary)-[:COVERS_CHAPTERS]->(Chapter)
     */
    public static final String COVERS_CHAPTERS = "COVERS_CHAPTERS";
    
    // ==================== 精准还原关系类型 ====================
    
    /**
     * 顺序关系，连接所有句子级节点，形成线性链
     * (SentenceNode)-[:NEXT_SENTENCE {sequenceNumber: Integer}]->(SentenceNode)
     */
    public static final String NEXT_SENTENCE = "NEXT_SENTENCE";
    
    /**
     * 说话关系，连接实体与其说出的对话
     * (Entity)-[:SPEAKS {sequenceNumber: Integer}]->(DialogueNode)
     */
    public static final String SPEAKS = "SPEAKS";
    
    /**
     * 执行动作，连接实体与其执行的动作事件
     * (Entity)-[:PERFORMS {sequenceNumber: Integer}]->(ActionEvent)
     */
    public static final String PERFORMS = "PERFORMS";
    
    /**
     * 动作客体，连接动作事件与动作指向的实体
     * (ActionEvent)-[:TARGETS]->(Entity)
     */
    public static final String TARGETS = "TARGETS";
    
    /**
     * 描写关系，连接描写节点与被描写的实体
     * (DescriptionNode)-[:DESCRIBES]->(Entity)
     */
    public static final String DESCRIBES = "DESCRIBES";
    
    /**
     * 心理活动，连接实体与其心理活动
     * (Entity)-[:THINKS {sequenceNumber: Integer}]->(ThoughtNode)
     */
    public static final String THINKS = "THINKS";
    
    /**
     * 叙述说明，连接章节与叙述节点
     * (Chapter)-[:NARRATES {sequenceNumber: Integer}]->(NarrationNode)
     */
    public static final String NARRATES = "NARRATES";
    
    private RelationshipType() {
        // 工具类，防止实例化
    }
}
