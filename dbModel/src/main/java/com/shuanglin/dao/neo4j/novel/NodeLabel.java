package com.shuanglin.dao.neo4j.novel;

/**
 * Neo4j节点标签常量定义
 * 对应systemPrompt-3.0.md第五章核心节点类型定义
 */
public class NodeLabel {
    
    /**
     * 章节节点标签
     */
    public static final String CHAPTER = "Chapter";
    
    /**
     * 事件节点标签
     */
    public static final String EVENT = "Event";
    
    /**
     * 实体节点标签（父标签）
     */
    public static final String ENTITY = "Entity";
    
    /**
     * 状态节点标签
     */
    public static final String STATE = "State";
    
    /**
     * 摘要节点标签
     */
    public static final String SUMMARY = "Summary";
    
    // ==================== 领域实体子类型标签 ====================
    
    /**
     * 角色节点标签（复合标签：:Entity:Character）
     */
    public static final String CHARACTER = "Character";
    
    /**
     * 地点节点标签（复合标签：:Entity:Location）
     */
    public static final String LOCATION = "Location";
    
    /**
     * 组织节点标签（复合标签：:Entity:Organization）
     */
    public static final String ORGANIZATION = "Organization";
    
    /**
     * 物品节点标签（复合标签：:Entity:Item）
     */
    public static final String ITEM = "Item";
    
    /**
     * 技能节点标签（复合标签：:Entity:Skill）
     */
    public static final String SKILL = "Skill";
    
    // ==================== 精准还原支持节点标签 ====================
    
    /**
     * 对话节点标签 - 用于精准还原小说对话
     */
    public static final String DIALOGUE_NODE = "DialogueNode";
    
    /**
     * 动作事件节点标签 - 用于精准还原句子级动作
     */
    public static final String ACTION_EVENT = "ActionEvent";
    
    /**
     * 描写节点标签 - 用于精准还原描写性内容
     */
    public static final String DESCRIPTION_NODE = "DescriptionNode";
    
    /**
     * 心理活动节点标签 - 用于精准还原角色心理
     */
    public static final String THOUGHT_NODE = "ThoughtNode";
    
    /**
     * 叙述节点标签 - 用于精准还原叙述性说明
     */
    public static final String NARRATION_NODE = "NarrationNode";
    
    private NodeLabel() {
        // 工具类，防止实例化
    }
}
