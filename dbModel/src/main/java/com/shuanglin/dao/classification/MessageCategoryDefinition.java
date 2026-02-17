package com.shuanglin.dao.classification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * 分类标签定义实体
 */
@Document("message_category_definition")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageCategoryDefinition {

    @Id
    private String id;

    /**
     * 分类编码
     */
    private String code;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 分类描述
     */
    private String description;

    /**
     * 关键词
     */
    private List<String> keywords;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 是否启用
     */
    private Boolean enabled;
}
