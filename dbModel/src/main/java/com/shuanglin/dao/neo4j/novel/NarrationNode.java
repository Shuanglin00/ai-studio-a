package com.shuanglin.dao.neo4j.novel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 叙述节点实体类
 * 用于精准还原功能的叙述性说明建模
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NarrationNode {
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
     * 叙述类型："time_transition"/"scene_change"/"explanation"/"summary"
     */
    private String narrativeType;
    
    /**
     * 所属书籍UUID
     */
    private String bookUuid;
    
    /**
     * 关联MongoDB sentence_units的文档ID
     */
    private String mongoDocId;
}
