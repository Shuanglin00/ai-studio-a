package com.shuanglin.mcp.tool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shuanglin.dao.classification.CollectedMessage;
import com.shuanglin.dao.classification.MessageCollectionBatch;
import com.shuanglin.dao.classification.enums.MessageCategory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 消息分类工具
 * 提供消息批次分类功能
 */
@Slf4j
@Component
public class MessageClassificationTool {

    private final ObjectMapper objectMapper;

    // 简单的关键词匹配分类规则
    private static final Map<MessageCategory, List<String>> CATEGORY_KEYWORDS = new HashMap<>();

    static {
        CATEGORY_KEYWORDS.put(MessageCategory.MEME, Arrays.asList("哈", "笑", "图", "梗", "草", "抽象", "蚌埠", "弔"));
        CATEGORY_KEYWORDS.put(MessageCategory.HELL_JOKE, Arrays.asList("地狱", "死", "坟", "功德", "木鱼", "阴间", "不敢笑"));
        CATEGORY_KEYWORDS.put(MessageCategory.REGIONAL_BLACK, Arrays.asList("你们那", "听说你们", "经典", "不愧是", "地域", "特色"));
        CATEGORY_KEYWORDS.put(MessageCategory.SPAM, Arrays.asList("？？？", "！！！", "111", "666", "???", "!!!"));
        CATEGORY_KEYWORDS.put(MessageCategory.AD, Arrays.asList("加群", "了解一下", "特价", "优惠", "包邮", "限时", "抢购"));
    }

    public MessageClassificationTool() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 对消息批次进行分类
     *
     * @param batchJson 批次数据JSON
     * @return 分类结果JSON
     */
    @Tool(name = "classifyMessageBatch",
            description = "对收集的消息批次进行自动分类（基于关键词匹配），返回分类结果")
    public String classifyMessageBatch(
            @ToolParam(description = "消息批次JSON") String batchJson) {
        try {
            MessageCollectionBatch batch = parseBatch(batchJson);

            // 基于关键词匹配进行分类
            ClassificationResult result = classifyByKeywords(batch);

            return objectMapper.writeValueAsString(result);

        } catch (Exception e) {
            log.error("Classification failed", e);
            return "{\"error\": \"Classification failed: " + e.getMessage() + "\"}";
        }
    }

    /**
     * 基于关键词匹配进行分类
     */
    private ClassificationResult classifyByKeywords(MessageCollectionBatch batch) {
        if (batch.getMessages() == null || batch.getMessages().isEmpty()) {
            return new ClassificationResult("normal", "正常", 0.5, "无消息内容");
        }

        // 统计每个分类的关键词命中次数
        Map<MessageCategory, Integer> categoryScores = new HashMap<>();

        for (CollectedMessage message : batch.getMessages()) {
            String content = message.getContent();
            if (content == null) continue;

            for (Map.Entry<MessageCategory, List<String>> entry : CATEGORY_KEYWORDS.entrySet()) {
                for (String keyword : entry.getValue()) {
                    if (content.contains(keyword)) {
                        categoryScores.merge(entry.getKey(), 1, Integer::sum);
                    }
                }
            }
        }

        // 找出得分最高的分类
        MessageCategory bestCategory = MessageCategory.NORMAL;
        int maxScore = 0;

        for (Map.Entry<MessageCategory, Integer> entry : categoryScores.entrySet()) {
            if (entry.getValue() > maxScore) {
                maxScore = entry.getValue();
                bestCategory = entry.getKey();
            }
        }

        // 计算置信度
        double confidence = Math.min(0.5 + (maxScore * 0.1), 0.95);

        String reasoning = maxScore > 0
                ? String.format("检测到%d个%s相关关键词", maxScore, bestCategory.getName())
                : "未检测到明显特征，归类为正常";

        return new ClassificationResult(
                bestCategory.getCode(),
                bestCategory.getName(),
                confidence,
                reasoning
        );
    }

    /**
     * 解析批次JSON
     */
    private MessageCollectionBatch parseBatch(String batchJson) throws JsonProcessingException {
        return objectMapper.readValue(batchJson, MessageCollectionBatch.class);
    }

    /**
     * 分类结果内部类
     */
    private static class ClassificationResult {
        public String primaryCategory;
        public String categoryName;
        public double confidence;
        public String reasoning;

        public ClassificationResult(String primaryCategory, String categoryName, double confidence, String reasoning) {
            this.primaryCategory = primaryCategory;
            this.categoryName = categoryName;
            this.confidence = confidence;
            this.reasoning = reasoning;
        }
    }
}
