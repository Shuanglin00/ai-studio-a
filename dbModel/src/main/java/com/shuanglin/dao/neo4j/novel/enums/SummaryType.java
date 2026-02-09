package com.shuanglin.dao.neo4j.novel.enums;

import lombok.Getter;

/**
 * 摘要类型枚举
 * 对应systemPrompt-3.0.md第五章Summary节点的summaryType属性定义
 */
@Getter
public enum SummaryType {
    
    /**
     * 人物小传
     */
    CHARACTER_BIOGRAPHY("人物小传", "CharacterBiography"),
    
    /**
     * 情节概述
     */
    PLOT_OVERVIEW("情节概述", "PlotOverview"),
    
    /**
     * 关系演变
     */
    RELATIONSHIP_EVOLUTION("关系演变", "RelationshipEvolution"),
    
    /**
     * 章节摘要
     */
    CHAPTER_SUMMARY("章节摘要", "ChapterSummary"),
    
    /**
     * 世界观设定
     */
    WORLD_SETTING("世界观设定", "WorldSetting");
    
    /**
     * 中文名称
     */
    private final String zhName;
    
    /**
     * 英文代码
     */
    private final String enCode;
    
    SummaryType(String zhName, String enCode) {
        this.zhName = zhName;
        this.enCode = enCode;
    }
    
    /**
     * 根据中文名称获取枚举
     */
    public static SummaryType fromZhName(String zhName) {
        for (SummaryType type : values()) {
            if (type.zhName.equals(zhName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的摘要类型: " + zhName);
    }
    
    /**
     * 根据英文代码获取枚举
     */
    public static SummaryType fromEnCode(String enCode) {
        for (SummaryType type : values()) {
            if (type.enCode.equalsIgnoreCase(enCode)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的摘要类型: " + enCode);
    }
}
