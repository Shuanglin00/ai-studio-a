package com.shuanglin.ai.langchain4j.controller.dto;

import lombok.Data;

import java.util.List;

/**
 * 小说知识图谱构建响应
 */
@Data
public class NovelResponse {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 消息
     */
    private String message;

    /**
     * 书籍UUID
     */
    private String bookUuid;

    /**
     * 书籍名称
     */
    private String bookName;

    /**
     * 总章节数
     */
    private Integer totalChapters;

    /**
     * 成功处理章节数
     */
    private Integer successChapters;

    /**
     * 失败章节数
     */
    private Integer failedChapters;

    /**
     * 跳过的章节数
     */
    private Integer skippedChapters;

    /**
     * 总耗时（毫秒）
     */
    private Long totalDuration;

    /**
     * 平均每章节耗时（毫秒）
     */
    private Long avgChapterDuration;

    /**
     * 数据统计信息
     */
    private String stats;

    /**
     * 创建成功响应
     */
    public static NovelResponse success(String bookUuid, String message) {
        NovelResponse response = new NovelResponse();
        response.setSuccess(true);
        response.setBookUuid(bookUuid);
        response.setMessage(message);
        return response;
    }

    /**
     * 创建失败响应
     */
    public static NovelResponse fail(String message) {
        NovelResponse response = new NovelResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }
}
