package com.shuanglin.dao.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 章节重建验证报告模型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidationReport {
    /**
     * 字符级相似度（0.0-1.0）
     */
    private double charSimilarity;
    
    /**
     * 句子级匹配率（0.0-1.0）
     */
    private double sentenceMatchRate;
    
    /**
     * 长度差异（字符数）
     */
    private int lengthDifference;
    
    /**
     * 是否通过验证
     */
    private boolean passed;
    
    /**
     * 差异详情列表
     */
    private List<String> differences;
    
    /**
     * 验证时间
     */
    private Date validatedAt;
    
    /**
     * 添加差异项
     */
    public void addDifference(String diff) {
        if (differences == null) {
            differences = new ArrayList<>();
        }
        differences.add(diff);
    }
    
    /**
     * 判断是否达标
     */
    public boolean meetsStandard() {
        return charSimilarity >= 0.95 && sentenceMatchRate == 1.0;
    }
    
    /**
     * 生成验证摘要
     */
    public String getSummary() {
        return String.format(
            "字符相似度: %.2f%%, 句子匹配率: %.2f%%, 长度差异: %d字符, 结果: %s",
            charSimilarity * 100,
            sentenceMatchRate * 100,
            lengthDifference,
            passed ? "通过" : "未通过"
        );
    }
}
