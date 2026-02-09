package com.shuanglin.dao.neo4j.novel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 描写节点实体类
 * 用于精准还原功能的描写内容建模
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DescriptionNode {
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
     * 描写类型："environment"/"appearance"/"emotion"/"scene"
     */
    private String descriptionType;
    
    /**
     * 被描写的实体ID
     */
    private String target;
    
    /**
     * 所属书籍UUID
     */
    private String bookUuid;
    
    /**
     * 关联MongoDB sentence_units的文档ID
     */
    private String mongoDocId;
}
