package com.shuanglin.dao.neo4j.novel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 心理活动节点实体类
 * 用于精准还原功能的心理活动内容建模
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThoughtNode {
    /**
     * 全局唯一标识符
     */
    private String uuid;
    
    /**
     * 所属章节索引
     */
    private Integer chapterIndex;
    
    /**
     * 全局顺序号（核心字段）
     */
    private Integer sequenceNumber;
    
    /**
     * 完整原文
     */
    private String sourceText;
    
    /**
     * 思考者实体ID
     */
    private String thinker;
    
    /**
     * 心理活动内容
     */
    private String content;
    
    /**
     * 所属书籍UUID
     */
    private String bookUuid;
    
    /**
     * 关联MongoDB sentence_units的文档ID
     */
    private String mongoDocId;
}
