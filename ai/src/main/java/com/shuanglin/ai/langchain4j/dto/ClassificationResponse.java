package com.shuanglin.ai.langchain4j.dto;

import com.shuanglin.dao.classification.enums.MessageCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分类响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassificationResponse {

    /**
     * 主分类
     */
    private MessageCategory primaryCategory;

    /**
     * 子分类列表
     */
    private List<String> subCategories;

    /**
     * 置信度 0-1
     */
    private Double confidence;

    /**
     * 分类理由
     */
    private String reasoning;

    /**
     * 关键指标
     */
    private List<String> keyIndicators;
}
