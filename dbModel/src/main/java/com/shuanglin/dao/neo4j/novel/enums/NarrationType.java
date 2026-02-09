package com.shuanglin.dao.neo4j.novel.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 叙述类型枚举
 * 用于精准还原功能的叙述节点分类
 */
@Getter
@AllArgsConstructor
public enum NarrationType {
    /**
     * 时间过渡
     */
    TIME_TRANSITION("时间过渡", "TimeTransition"),
    
    /**
     * 场景转换
     */
    SCENE_CHANGE("场景转换", "SceneChange"),
    
    /**
     * 说明性文字
     */
    EXPLANATION("说明", "Explanation"),
    
    /**
     * 摘要总结
     */
    SUMMARY("摘要", "Summary");
    
    /**
     * 中文名称
     */
    private final String zhName;
    
    /**
     * 英文代码
     */
    private final String enCode;
}
