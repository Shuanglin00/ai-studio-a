package com.shuanglin.dao.neo4j.novel.enums;

import lombok.Getter;

/**
 * 状态类型枚举
 * 对应systemPrompt-3.0.md第五章State节点的stateType属性定义
 */
@Getter
public enum StateType {
    
    /**
     * 境界状态
     */
    REALM("境界", "Realm"),
    
    /**
     * 健康状况
     */
    HEALTH("健康状况", "Health"),
    
    /**
     * 地理位置
     */
    LOCATION("地理位置", "Location"),
    
    /**
     * 身份状态
     */
    IDENTITY("身份", "Identity"),
    
    /**
     * 持有物品
     */
    POSSESSION("持有物品", "Possession"),
    
    /**
     * 情绪状态
     */
    EMOTION("情绪", "Emotion"),
    
    /**
     * 关系状态
     */
    RELATIONSHIP("关系", "Relationship"),
    
    /**
     * 能力状态
     */
    ABILITY("能力", "Ability"),
    
    /**
     * 声望状态
     */
    REPUTATION("声望", "Reputation"),
    
    /**
     * 其他状态
     */
    OTHER("其他", "Other");
    
    /**
     * 中文名称
     */
    private final String zhName;
    
    /**
     * 英文代码
     */
    private final String enCode;
    
    StateType(String zhName, String enCode) {
        this.zhName = zhName;
        this.enCode = enCode;
    }
    
    /**
     * 根据中文名称获取枚举
     */
    public static StateType fromZhName(String zhName) {
        for (StateType type : values()) {
            if (type.zhName.equals(zhName)) {
                return type;
            }
        }
        return OTHER;
    }
    
    /**
     * 根据英文代码获取枚举
     */
    public static StateType fromEnCode(String enCode) {
        for (StateType type : values()) {
            if (type.enCode.equalsIgnoreCase(enCode)) {
                return type;
            }
        }
        return OTHER;
    }
}
