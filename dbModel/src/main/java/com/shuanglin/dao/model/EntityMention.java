package com.shuanglin.dao.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 实体提及模型
 * 用于全局扫描阶段的临时数据结构
 * 该类不对应MongoDB集合，仅在内存中使用
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntityMention {
    /**
     * 标准名称
     */
    private String standardName;
    
    /**
     * 别名列表
     */
    private List<String> aliases;
    
    /**
     * 实体类型：Character/Location/Organization/Item/Skill
     */
    private String type;
    
    /**
     * 首次提及的文本片段
     */
    private String firstMention;
    
    /**
     * 置信度（0.0-1.0）
     */
    private Double confidence;
    
    /**
     * 获取所有名称（标准名+别名）
     */
    public List<String> getAllNames() {
        List<String> allNames = new ArrayList<>();
        allNames.add(standardName);
        if (aliases != null) {
            allNames.addAll(aliases);
        }
        return allNames;
    }
    
    /**
     * 从JSON解析
     */
    @SuppressWarnings("unchecked")
    public static EntityMention fromJson(Map<String, Object> json) {
        EntityMention mention = new EntityMention();
        mention.setStandardName((String) json.get("standardName"));
        mention.setAliases((List<String>) json.get("aliases"));
        mention.setType((String) json.get("entityType"));
        mention.setFirstMention((String) json.get("firstMention"));
        mention.setConfidence((Double) json.getOrDefault("confidence", 1.0));
        return mention;
    }
}
