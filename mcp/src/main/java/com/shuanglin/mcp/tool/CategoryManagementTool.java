package com.shuanglin.mcp.tool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shuanglin.dao.classification.MessageCategoryDefinition;
import com.shuanglin.dao.classification.enums.MessageCategory;
import com.shuanglin.dao.classification.repository.MessageCategoryDefinitionRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 分类管理工具
 * 提供分类标签的查询和管理功能
 */
@Component
public class CategoryManagementTool {

    private final MessageCategoryDefinitionRepository categoryRepository;
    private final ObjectMapper objectMapper;

    // 默认分类定义
    private static final List<DefaultCategory> DEFAULT_CATEGORIES = Arrays.asList(
            new DefaultCategory("meme", "弔图", "搞笑图片、表情包、段子", false, 1),
            new DefaultCategory("hell-joke", "地狱笑话", "黑色幽默、涉及死亡/疾病的笑话", true, 2),
            new DefaultCategory("regional-black", "地域黑", "针对特定地区的负面言论或刻板印象", true, 3),
            new DefaultCategory("political", "政治敏感", "涉及政治人物、政策、敏感历史事件", true, 4),
            new DefaultCategory("nsfw", "NSFW", "不适宜工作场所的内容（色情、暴力等）", true, 5),
            new DefaultCategory("normal", "正常", "普通聊天内容", false, 10),
            new DefaultCategory("spam", "刷屏", "无意义的重复内容、纯表情刷屏", false, 6),
            new DefaultCategory("ad", "广告", "商业广告、推广信息", false, 7),
            new DefaultCategory("other", "其他", "其他未分类内容", false, 20)
    );

    @Autowired
    public CategoryManagementTool(MessageCategoryDefinitionRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 构造函数用于测试
     */
    public CategoryManagementTool(MessageCategoryDefinitionRepository categoryRepository, ObjectMapper objectMapper) {
        this.categoryRepository = categoryRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * 列出所有分类
     *
     * @return 分类列表JSON
     */
    @Tool(name = "listCategories",
            description = "列出所有可用的消息分类标签")
    public String listCategories() {
        if (categoryRepository != null) {
            try {
                var categories = categoryRepository.findByEnabledTrueOrderByPriorityAsc();
                return objectMapper.writeValueAsString(categories);
            } catch (JsonProcessingException e) {
                return getDefaultCategoriesJson();
            }
        }
        return getDefaultCategoriesJson();
    }

    /**
     * 获取分类详情
     *
     * @param categoryCode 分类编码
     * @return 分类详情JSON
     */
    @Tool(name = "getCategoryDetails",
            description = "获取指定分类的详细信息")
    public String getCategoryDetails(
            @ToolParam(description = "分类编码") String categoryCode) {
        if (categoryCode == null || categoryCode.isBlank()) {
            return "{\"error\": \"Category code is required\"}";
        }

        // 从默认分类中查找
        for (DefaultCategory cat : DEFAULT_CATEGORIES) {
            if (cat.code.equals(categoryCode)) {
                try {
                    return objectMapper.writeValueAsString(cat);
                } catch (JsonProcessingException e) {
                    return "{\"error\": \"Failed to serialize\"}";
                }
            }
        }

        // 从数据库查找
        if (categoryRepository != null) {
            var category = categoryRepository.findByCode(categoryCode);
            if (category.isPresent()) {
                try {
                    return objectMapper.writeValueAsString(category.get());
                } catch (JsonProcessingException e) {
                    return "{\"error\": \"Failed to serialize\"}";
                }
            }
        }

        return "{\"error\": \"Category not found\", \"code\": \"" + categoryCode + "\"}";
    }

    /**
     * 获取分类描述
     *
     * @param categoryCode 分类编码
     * @return 分类描述
     */
    @Tool(name = "getCategoryDescription",
            description = "获取指定分类的描述信息")
    public String getCategoryDescription(
            @ToolParam(description = "分类编码") String categoryCode) {
        for (DefaultCategory cat : DEFAULT_CATEGORIES) {
            if (cat.code.equals(categoryCode)) {
                return cat.description;
            }
        }

        if (categoryRepository != null) {
            var category = categoryRepository.findByCode(categoryCode);
            if (category.isPresent()) {
                return category.get().getDescription();
            }
        }

        return "Unknown category";
    }

    /**
     * 验证分类编码是否有效
     *
     * @param categoryCode 分类编码
     * @return 是否有效
     */
    @Tool(name = "isValidCategory",
            description = "验证分类编码是否有效")
    public boolean isValidCategory(
            @ToolParam(description = "分类编码") String categoryCode) {
        if (categoryCode == null || categoryCode.isBlank()) {
            return false;
        }

        // 检查默认分类
        for (DefaultCategory cat : DEFAULT_CATEGORIES) {
            if (cat.code.equals(categoryCode)) {
                return true;
            }
        }

        // 检查数据库
        if (categoryRepository != null) {
            return categoryRepository.existsByCode(categoryCode);
        }

        return false;
    }

    /**
     * 判断是否为敏感分类
     *
     * @param categoryCode 分类编码
     * @return 是否敏感
     */
    @Tool(name = "isSensitiveCategory",
            description = "判断指定分类是否为敏感分类")
    public boolean isSensitiveCategory(
            @ToolParam(description = "分类编码") String categoryCode) {
        for (DefaultCategory cat : DEFAULT_CATEGORIES) {
            if (cat.code.equals(categoryCode)) {
                return cat.sensitive;
            }
        }

        try {
            MessageCategory category = MessageCategory.fromCode(categoryCode);
            return category.isSensitive();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 获取所有敏感分类
     *
     * @return 敏感分类编码列表
     */
    @Tool(name = "getSensitiveCategories",
            description = "获取所有敏感分类的编码列表")
    public List<String> getSensitiveCategories() {
        List<String> sensitive = new ArrayList<>();
        for (DefaultCategory cat : DEFAULT_CATEGORIES) {
            if (cat.sensitive) {
                sensitive.add(cat.code);
            }
        }
        return sensitive;
    }

    /**
     * 获取分类优先级
     *
     * @param categoryCode 分类编码
     * @return 优先级（数字越小优先级越高，-1表示无效分类）
     */
    public int getCategoryPriority(String categoryCode) {
        for (DefaultCategory cat : DEFAULT_CATEGORIES) {
            if (cat.code.equals(categoryCode)) {
                return cat.priority;
            }
        }
        return -1;
    }

    /**
     * 获取默认分类JSON
     */
    private String getDefaultCategoriesJson() {
        try {
            return objectMapper.writeValueAsString(DEFAULT_CATEGORIES);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    /**
     * 默认分类内部类
     */
    private static class DefaultCategory {
        public String code;
        public String name;
        public String description;
        public boolean sensitive;
        public int priority;

        public DefaultCategory(String code, String name, String description, boolean sensitive, int priority) {
            this.code = code;
            this.name = name;
            this.description = description;
            this.sensitive = sensitive;
            this.priority = priority;
        }
    }
}
