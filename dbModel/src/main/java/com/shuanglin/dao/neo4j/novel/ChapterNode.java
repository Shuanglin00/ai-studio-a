package com.shuanglin.dao.neo4j.novel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 章节节点 - Neo4j实体类
 * 对应systemPrompt-3.0.md第五章节点定义
 * 
 * 定义：代表小说的一个章节，是时间轴的基本单位和内容的容器
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChapterNode {
    
    /**
     * 章节的唯一顺序编号
     */
    private Integer chapterIndex;
    
    /**
     * 章节标题
     */
    private String name;
    
    /**
     * 对本章内容的AI生成或人工编写的摘要
     */
    private String summary;
}
