package com.shuanglin.dao.neo4j.novel.enums;

import lombok.Getter;

/**
 * 事件类型枚举
 * 对应systemPrompt-3.0.md第五章Event节点的eventType属性定义
 */
@Getter
public enum EventType {
    
    /**
     * 对话事件
     */
    DIALOGUE("对话", "Dialogue"),
    
    /**
     * 战斗事件
     */
    BATTLE("战斗", "Battle"),
    
    /**
     * 相遇事件
     */
    ENCOUNTER("相遇", "Encounter"),
    
    /**
     * 决策事件
     */
    DECISION("决策", "Decision"),
    
    /**
     * 修炼事件
     */
    CULTIVATION("修炼", "Cultivation"),
    
    /**
     * 突破事件
     */
    BREAKTHROUGH("突破", "Breakthrough"),
    
    /**
     * 交易事件
     */
    TRANSACTION("交易", "Transaction"),
    
    /**
     * 移动事件
     */
    MOVEMENT("移动", "Movement"),
    
    /**
     * 获取物品事件
     */
    ACQUISITION("获取", "Acquisition"),
    
    /**
     * 学习技能事件
     */
    LEARNING("学习", "Learning"),
    
    /**
     * 其他事件
     */
    OTHER("其他", "Other");
    
    /**
     * 中文描述
     */
    private final String zhName;
    
    /**
     * 英文代码
     */
    private final String enCode;
    
    EventType(String zhName, String enCode) {
        this.zhName = zhName;
        this.enCode = enCode;
    }
    
    /**
     * 根据中文名称获取枚举
     */
    public static EventType fromZhName(String zhName) {
        for (EventType type : values()) {
            if (type.zhName.equals(zhName)) {
                return type;
            }
        }
        return OTHER;
    }
    
    /**
     * 根据英文代码获取枚举
     */
    public static EventType fromEnCode(String enCode) {
        for (EventType type : values()) {
            if (type.enCode.equalsIgnoreCase(enCode)) {
                return type;
            }
        }
        return OTHER;
    }
}
