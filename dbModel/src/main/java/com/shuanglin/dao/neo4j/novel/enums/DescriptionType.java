package com.shuanglin.dao.neo4j.novel.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 描写类型枚举
 * 用于精准还原功能的描写节点分类
 */
@Getter
@AllArgsConstructor
public enum DescriptionType {
    /**
     * 环境描写
     */
    ENVIRONMENT("环境", "Environment"),
    
    /**
     * 外貌描写
     */
    APPEARANCE("外貌", "Appearance"),
    
    /**
     * 情感描写
     */
    EMOTION("情感", "Emotion"),
    
    /**
     * 场景描写
     */
    SCENE("场景", "Scene");
    
    /**
     * 中文名称
     */
    private final String zhName;
    
    /**
     * 英文代码
     */
    private final String enCode;
}
