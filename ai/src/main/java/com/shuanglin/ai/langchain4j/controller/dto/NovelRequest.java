package com.shuanglin.ai.langchain4j.controller.dto;

import lombok.Data;

/**
 * 小说知识图谱构建请求
 */
@Data
public class NovelRequest {

    /**
     * EPUB 文件路径
     */
    private String filePath;

    /**
     * 书籍唯一标识
     */
    private String bookUuid;

    /**
     * 书籍名称
     */
    private String bookName;

    /**
     * 数据源标识
     */
    private String dataSource;

    /**
     * 章节数量限制（0表示不限制）
     */
    private Integer chapterLimit = 0;

    /**
     * 章节索引（用于重放）
     */
    private Integer chapterIndex;
}
