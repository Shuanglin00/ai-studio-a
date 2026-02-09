package com.shuanglin.dao.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 小说处理结果报告模型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessResult {
    /**
     * 处理是否成功
     */
    private boolean success;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 实体总数
     */
    private int entityCount;
    
    /**
     * 句子总数
     */
    private int sentenceCount;
    
    /**
     * 图谱节点总数
     */
    private int nodeCount;
    
    /**
     * 图谱关系总数
     */
    private int relationshipCount;
    
    /**
     * 各章节验证报告
     */
    private Map<Integer, ValidationReport> validationReports;
    
    /**
     * 处理总耗时（毫秒）
     */
    private long processingTimeMs;
    
    /**
     * 完成时间
     */
    private Date completedAt;
    
    /**
     * 添加章节验证报告
     */
    public void addValidationReport(int chapterIndex, ValidationReport report) {
        if (validationReports == null) {
            validationReports = new HashMap<>();
        }
        validationReports.put(chapterIndex, report);
    }
    
    /**
     * 获取平均字符相似度
     */
    public double getAverageCharSimilarity() {
        if (validationReports == null || validationReports.isEmpty()) {
            return 0.0;
        }
        
        double sum = 0.0;
        for (ValidationReport report : validationReports.values()) {
            sum += report.getCharSimilarity();
        }
        return sum / validationReports.size();
    }
    
    /**
     * 获取平均句子匹配率
     */
    public double getAverageSentenceMatchRate() {
        if (validationReports == null || validationReports.isEmpty()) {
            return 0.0;
        }
        
        double sum = 0.0;
        for (ValidationReport report : validationReports.values()) {
            sum += report.getSentenceMatchRate();
        }
        return sum / validationReports.size();
    }
    
    /**
     * 获取验证通过的章节数
     */
    public int getPassedChapterCount() {
        if (validationReports == null) {
            return 0;
        }
        
        int count = 0;
        for (ValidationReport report : validationReports.values()) {
            if (report.isPassed()) {
                count++;
            }
        }
        return count;
    }
    
    @Override
    public String toString() {
        return String.format(
            "ProcessResult[success=%s, entities=%d, sentences=%d, nodes=%d, relationships=%d, "
            + "avgCharSimilarity=%.2f%%, avgSentenceMatch=%.2f%%, passedChapters=%d/%d, time=%dms]",
            success,
            entityCount,
            sentenceCount,
            nodeCount,
            relationshipCount,
            getAverageCharSimilarity() * 100,
            getAverageSentenceMatchRate() * 100,
            getPassedChapterCount(),
            validationReports != null ? validationReports.size() : 0,
            processingTimeMs
        );
    }
}
