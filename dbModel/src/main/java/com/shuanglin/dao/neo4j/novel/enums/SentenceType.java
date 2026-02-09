package com.shuanglin.dao.neo4j.novel.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 句子类型枚举
 * 用于精准还原功能的句子级内容分类
 */
@Getter
@AllArgsConstructor
public enum SentenceType {
    /**
     * 对话
     */
    DIALOGUE("对话", "Dialogue"),
    
    /**
     * 动作
     */
    ACTION("动作", "Action"),
    
    /**
     * 描写
     */
    DESCRIPTION("描写", "Description"),
    
    /**
     * 心理活动
     */
    THOUGHT("心理活动", "Thought"),
    
    /**
     * 状态变化
     */
    STATE_CHANGE("状态变化", "StateChange"),
    
    /**
     * 叙述
     */
    NARRATION("叙述", "Narration");
    
    /**
     * 中文名称
     */
    private final String zhName;
    
    /**
     * 英文代码
     */
    private final String enCode;
}
