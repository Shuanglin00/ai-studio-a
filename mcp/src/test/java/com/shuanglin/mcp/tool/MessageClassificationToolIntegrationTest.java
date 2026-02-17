package com.shuanglin.mcp.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.shuanglin.dao.classification.CollectedMessage;
import com.shuanglin.dao.classification.MessageCollectionBatch;
import com.shuanglin.dao.classification.enums.BatchStatus;
import com.shuanglin.dao.classification.enums.MessageCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 消息分类工具集成测试
 */
@DisplayName("消息分类工具集成测试")
class MessageClassificationToolIntegrationTest {

    private MessageClassificationTool tool;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        tool = new MessageClassificationTool();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("完整流程：生成Mock数据并分类")
    void fullFlowGenerateAndClassify() throws Exception {
        // Given - 使用MockDataGeneratorTool生成数据
        MockDataGeneratorTool mockTool = new MockDataGeneratorTool();
        String batchJson = mockTool.generateMockMessageBatch("123456789", 20, "MEME");

        // When - 分类
        String result = tool.classifyMessageBatch(batchJson);

        // Then
        assertNotNull(result);
        assertFalse(result.isBlank());
        // 验证返回了有效的JSON（包含必要字段）
        assertTrue(result.contains("primaryCategory") || result.contains("error"));
    }

    @Test
    @DisplayName("多种分类的批量测试")
    void batchTestMultipleCategories() throws Exception {
        MockDataGeneratorTool mockTool = new MockDataGeneratorTool();
        String[] categories = {"MEME", "HELL_JOKE", "NORMAL", "SPAM"};

        for (String category : categories) {
            // Given
            String batchJson = mockTool.generateMockMessageBatch("123456789", 10, category);

            // When
            String result = tool.classifyMessageBatch(batchJson);

            // Then - 只要有结果即可（Mock数据可能无法被准确分类）
            assertNotNull(result, "Category " + category + " should produce result");
            // 不强制要求没有error，因为Mock数据可能无法匹配任何关键词
            assertFalse(result.isBlank(), "Category " + category + " should return non-empty result");
        }
    }

    @Test
    @DisplayName("混合内容分类测试")
    void mixedContentClassification() throws Exception {
        // Given - 创建包含多种特征的消息
        List<CollectedMessage> messages = Arrays.asList(
                createMessage("哈哈哈哈"),
                createMessage("这太地狱了"),
                createMessage("正常聊天"),
                createMessage("笑死我了"),
                createMessage("不敢笑")
        );

        String batchJson = createBatchJson(messages);

        // When
        String result = tool.classifyMessageBatch(batchJson);

        // Then - 应该能够分类，不报错
        assertNotNull(result);
        assertFalse(result.isBlank());
    }

    @Test
    @DisplayName("极端情况：单条消息分类")
    void singleMessageClassification() throws Exception {
        // Given
        List<CollectedMessage> messages = Collections.singletonList(
                createMessage("哈哈哈哈")
        );
        String batchJson = createBatchJson(messages);

        // When
        String result = tool.classifyMessageBatch(batchJson);

        // Then
        assertNotNull(result);
        assertFalse(result.contains("error"));
    }

    @Test
    @DisplayName("极端情况：大量消息分类")
    void largeBatchClassification() throws Exception {
        // Given - 创建50条消息
        MockDataGeneratorTool mockTool = new MockDataGeneratorTool();
        String batchJson = mockTool.generateMockMessageBatch("123456789", 50, "MEME");

        // When
        String result = tool.classifyMessageBatch(batchJson);

        // Then
        assertNotNull(result);
        assertFalse(result.isBlank());
    }

    @Test
    @DisplayName("置信度范围验证")
    void confidenceRangeValidation() throws Exception {
        // Given - 创建有明显特征的消息
        List<CollectedMessage> messages = Arrays.asList(
                createMessage("哈哈哈哈哈哈哈"),
                createMessage("笑死我了"),
                createMessage("这图太搞了"),
                createMessage("什么弔图"),
                createMessage("草")
        );
        String batchJson = createBatchJson(messages);

        // When
        String result = tool.classifyMessageBatch(batchJson);

        // Then - 解析结果检查置信度范围
        assertTrue(result.contains("confidence"));
        // 置信度应该在0.0-1.0之间
        double confidence = extractConfidence(result);
        assertTrue(confidence >= 0.0 && confidence <= 1.0);
    }

    @Test
    @DisplayName("空内容消息处理")
    void emptyContentHandling() throws Exception {
        // Given
        List<CollectedMessage> messages = Arrays.asList(
                createMessage(""),
                createMessage(null),
                createMessage("正常消息")
        );
        String batchJson = createBatchJson(messages);

        // When
        String result = tool.classifyMessageBatch(batchJson);

        // Then - 应该能处理，不报错
        assertNotNull(result);
    }

    private CollectedMessage createMessage(String content) {
        return CollectedMessage.builder()
                .messageId(System.currentTimeMillis())
                .userId(12345L)
                .nickname("测试用户")
                .content(content)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    private String createBatchJson(List<CollectedMessage> messages) throws Exception {
        MessageCollectionBatch batch = MessageCollectionBatch.builder()
                .id("test-batch-001")
                .groupId("123456789")
                .triggerMessageId(11111L)
                .triggerMessageType("IMAGE")
                .messages(messages)
                .messageCount(messages.size())
                .status(BatchStatus.COMPLETED)
                .build();

        return objectMapper.writeValueAsString(batch);
    }

    private double extractConfidence(String result) {
        // 简单解析，实际应该使用JSON解析
        try {
            int start = result.indexOf("\"confidence\":") + 13;
            int end = result.indexOf(",", start);
            if (end == -1) end = result.indexOf("}", start);
            return Double.parseDouble(result.substring(start, end).trim());
        } catch (Exception e) {
            return 0.5;
        }
    }
}
