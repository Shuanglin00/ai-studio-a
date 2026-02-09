package com.shuanglin.dao.neo4j.novel;

import com.shuanglin.dao.neo4j.novel.enums.SummaryType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 摘要节点 - Neo4j实体类
 * 对应systemPrompt-3.0.md第五章节点定义
 * 
 * 定义：跨越多个章节的、关于特定主题（如实体、关系、情节）的综合性信息，为RAG应用优化
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SummaryNode {
    
    /**
     * 摘要的唯一标识符
     */
    private String uuid;
    
    /**
     * 摘要类型
     */
    private SummaryType summaryType;
    
    /**
     * 摘要的详细文本内容
     */
    private String content;
    
    /**
     * 摘要涵盖的起始章节
     */
    private Integer startChapter;
    
    /**
     * 摘要涵盖的结束章节
     */
    private Integer endChapter;
    
    /**
     * "AI Generated" 或 "Human Annotated"
     */
    private String source;
}
