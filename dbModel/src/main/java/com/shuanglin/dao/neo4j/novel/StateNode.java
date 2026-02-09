package com.shuanglin.dao.neo4j.novel;

import com.shuanglin.dao.neo4j.novel.enums.StateType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 状态节点 - Neo4j实体类
 * 对应systemPrompt-3.0.md第五章节点定义
 * 
 * 定义：Entity在特定章节的最终属性快照
 * 是Event作用于Entity的结果，记录实体在某个章节结束时的最终状态
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StateNode {
    
    /**
     * 状态版本的唯一标识符
     */
    private String uuid;
    
    /**
     * 此状态生效的章节
     */
    private Integer chapterIndex;
    
    /**
     * 状态的分类
     */
    private StateType stateType;
    
    /**
     * 状态的具体值 (如 "斗皇", "重伤", "云岚宗", "宗主", "玄重尺")
     */
    private String stateValue;
    
    /**
     * 对状态的补充描述
     */
    private String description;
}
