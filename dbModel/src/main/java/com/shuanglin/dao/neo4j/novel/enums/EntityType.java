package com.shuanglin.dao.neo4j.novel.enums;

import lombok.Getter;

/**
 * 实体类型枚举
 * 对应systemPrompt-3.0.md第5.2节领域实体子类型定义
 */
@Getter
public enum EntityType {
    
    /**
     * 角色实体
     */
    CHARACTER("Character", "角色"),
    
    /**
     * 地点实体
     */
    LOCATION("Location", "地点"),
    
    /**
     * 组织实体
     */
    ORGANIZATION("Organization", "组织"),
    
    /**
     * 物品实体
     */
    ITEM("Item", "物品"),
    
    /**
     * 技能实体
     */
    SKILL("Skill", "技能");
    
    /**
     * 类型代码（用于Neo4j标签和entityType属性）
     */
    private final String code;
    
    /**
     * 类型描述
     */
    private final String description;
    
    EntityType(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    /**
     * 根据代码获取枚举
     */
    public static EntityType fromCode(String code) {
        for (EntityType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的实体类型: " + code);
    }
}
