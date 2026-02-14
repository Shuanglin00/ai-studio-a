package com.shuanglin.ai.novel.service;

import com.shuanglin.ai.novel.enums.EntityTypeEnum;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 生成提示模板
 * 负责构建基于知识图谱的上下文和提示词
 */
@Slf4j
@Component
public class GenerationPrompt {

    /**
     * 构建续写模式的系统提示
     * @param context 上下文信息
     * @return 格式化后的提示
     */
    public String buildContinueSystemPrompt(GenerationContext context) {
        return String.format("""
                # 小说续写任务

                你是一个小说作家，擅长根据已有剧情续写新章节。

                ## 书籍信息
                - 书名：%s
                - 当前写作风格：%s

                ## 写作要求
                1. 保持与原文一致的语言风格和叙事节奏
                2. 确保人物性格和关系发展合理
                3. 情节发展应该自然流畅，符合故事逻辑
                4. 新章节应该在已有剧情基础上推进
                5. 保持章节长度与原文相近

                ## 人物状态
                %s

                ## 历史事件
                %s

                请根据以上信息续写新章节。
                """,
                context.getBookName(),
                context.getWritingStyle(),
                formatCharacterStates(context.getCharacterStates()),
                formatHistoricalEvents(context.getHistoricalEvents())
        );
    }

    /**
     * 构建概要模式的系统提示
     * @param context 上下文信息
     * @param outline 用户提供的情节概要
     * @return 格式化后的提示
     */
    public String buildOutlineSystemPrompt(GenerationContext context, String outline) {
        return String.format("""
                # 小说生成任务（概要模式）

                你是一个小说作家，擅长根据情节概要补充细节。

                ## 书籍信息
                - 书名：%s
                - 当前写作风格：%s

                ## 用户提供的情节概要
                %s

                ## 写作要求
                1. 根据概要展开细节描写
                2. 保持与原文一致的语言风格
                3. 合理运用人物性格和关系
                4. 确保情节逻辑通顺

                ## 人物状态
                %s

                ## 历史事件
                %s

                请根据概要生成详细内容。
                """,
                context.getBookName(),
                context.getWritingStyle(),
                outline,
                formatCharacterStates(context.getCharacterStates()),
                formatHistoricalEvents(context.getHistoricalEvents())
        );
    }

    /**
     * 构建混合模式的系统提示
     * @param context 上下文信息
     * @param content 续写内容
     * @param outline 补充概要
     * @return 格式化后的提示
     */
    public String buildHybridSystemPrompt(GenerationContext context, String content, String outline) {
        return String.format("""
                # 小说生成任务（混合模式）

                你是一个小说作家，结合续写和概要进行创作。

                ## 书籍信息
                - 书名：%s
                - 当前写作风格：%s

                ## 续写内容
                %s

                ## 补充概要
                %s

                ## 写作要求
                1. 将续写内容和概要自然融合
                2. 保持与原文一致的语言风格
                3. 确保情节逻辑通顺
                4. 新章节应该自然衔接前文

                ## 人物状态
                %s

                ## 历史事件
                %s

                请生成完整的章节内容。
                """,
                context.getBookName(),
                context.getWritingStyle(),
                content,
                outline,
                formatCharacterStates(context.getCharacterStates()),
                formatHistoricalEvents(context.getHistoricalEvents())
        );
    }

    /**
     * 构建用户提示（续写模式）
     * @param context 上下文信息
     * @param prompt 用户提示
     * @return 用户提示文本
     */
    public String buildContinueUserPrompt(GenerationContext context, ChapterPrompt prompt) {
        StringBuilder sb = new StringBuilder();

        // 添加最近章节摘要
        if (prompt.getRecentChaptersSummary() != null) {
            sb.append("## 最近剧情\n");
            sb.append(prompt.getRecentChaptersSummary());
            sb.append("\n\n");
        }

        // 添加续写要求
        if (prompt.getChapterTitle() != null) {
            sb.append("## 章节标题\n");
            sb.append(prompt.getChapterTitle());
            sb.append("\n\n");
        }

        if (prompt.getChapterLength() > 0) {
            sb.append("## 期望长度\n");
            sb.append(prompt.getChapterLength()).append("字\n\n");
        }

        sb.append("请续写新章节内容：");

        return sb.toString();
    }

    /**
     * 构建用户提示（概要模式）
     * @param context 上下文信息
     * @param prompt 用户提示
     * @param outline 情节概要
     * @return 用户提示文本
     */
    public String buildOutlineUserPrompt(GenerationContext context, ChapterPrompt prompt, String outline) {
        StringBuilder sb = new StringBuilder();

        sb.append("## 情节概要\n");
        sb.append(outline);
        sb.append("\n\n");

        if (prompt.getChapterLength() > 0) {
            sb.append("## 期望长度\n");
            sb.append(prompt.getChapterLength()).append("字\n\n");
        }

        sb.append("请根据概要生成详细内容：");

        return sb.toString();
    }

    /**
     * 构建实体状态提示（支持20种实体类型）
     * @param characterStates 实体状态列表
     * @return 格式化后的状态文本
     */
    private String formatCharacterStates(List<CharacterState> characterStates) {
        if (characterStates == null || characterStates.isEmpty()) {
            return "无";
        }

        return characterStates.stream()
                .map(state -> {
                    StringBuilder sb = new StringBuilder();
                    sb.append("- ").append(state.getEntityName());

                    // 添加实体类型
                    if (state.getEntityType() != null) {
                        sb.append("[").append(state.getEntityType().getName()).append("]");
                    }

                    // 添加各类状态信息
                    if (state.getRealm() != null) {
                        sb.append("：境界=").append(state.getRealm());
                    }
                    if (state.getLocation() != null) {
                        sb.append("，位置=").append(state.getLocation());
                    }
                    if (state.getProfession() != null) {
                        sb.append("，职业=").append(state.getProfession());
                    }
                    if (state.getOrganization() != null) {
                        sb.append("，所属=").append(state.getOrganization());
                    }
                    if (state.getSkill() != null) {
                        sb.append("，技能=").append(state.getSkill());
                    }
                    if (state.getItem() != null) {
                        sb.append("，物品=").append(state.getItem());
                    }

                    return sb.toString();
                })
                .collect(Collectors.joining("\n"));
    }

    /**
     * 构建历史事件提示
     * @param historicalEvents 历史事件列表
     * @return 格式化后的事件文本
     */
    private String formatHistoricalEvents(List<HistoricalEvent> historicalEvents) {
        if (historicalEvents == null || historicalEvents.isEmpty()) {
            return "无";
        }

        return historicalEvents.stream()
                .limit(10) // 限制显示最近10个事件
                .map(event -> String.format("- 第%d章：%s - %s",
                        event.getChapterIndex(),
                        event.getEventType(),
                        event.getDescription()))
                .collect(Collectors.joining("\n"));
    }

    /**
     * 生成上下文
     */
    @Data
    public static class GenerationContext {
        private String bookUuid;
        private String bookName;
        private String writingStyle;
        private List<CharacterState> characterStates;
        private List<HistoricalEvent> historicalEvents;
        private List<LocationInfo> locations;
    }

    /**
     * 实体状态（支持20种实体类型）
     */
    @Data
    public static class CharacterState {
        private String entityName;
        private EntityTypeEnum entityType;  // 支持20种实体类型
        private String realm;              // 境界/等级 (REALM)
        private String location;           // 地点 (LOCATION)
        private String health;
        private String emotion;
        private String profession;        // 职业 (PROFESSION)
        private String organization;       // 所属组织 (ORGANIZATION)
        private String skill;              // 技能 (SKILL)
        private String item;               // 物品 (ITEM)
        private int chapterIndex;

        // 兼容旧方法
        public String getCharacterName() {
            return entityName;
        }

        public void setCharacterName(String name) {
            this.entityName = name;
        }
    }

    /**
     * 历史事件
     */
    @Data
    public static class HistoricalEvent {
        private String eventUuid;
        private int chapterIndex;
        private String eventType;
        private String description;
        private List<String> participants;
    }

    /**
     * 地点信息
     */
    @Data
    public static class LocationInfo {
        private String locationName;
        private String description;
        private List<String> connectedLocations;
    }

    /**
     * 章节生成提示
     */
    @Data
    public static class ChapterPrompt {
        private String recentChaptersSummary;
        private String chapterTitle;
        private int chapterLength;
        private Map<String, String> customVariables;

        public ChapterPrompt() {
            this.customVariables = new HashMap<>();
        }

        public void addVariable(String key, String value) {
            this.customVariables.put(key, value);
        }

        public String getVariable(String key) {
            return this.customVariables.get(key);
        }
    }
}
