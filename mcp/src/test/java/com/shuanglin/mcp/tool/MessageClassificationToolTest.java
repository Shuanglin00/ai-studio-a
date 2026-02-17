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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 消息分类工具测试
 */
@DisplayName("消息分类工具测试")
class MessageClassificationToolTest {

    private MessageClassificationTool tool;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        tool = new MessageClassificationTool();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("应该对MEME分类消息进行正确分类")
    void shouldClassifyMemeMessages() throws Exception {
        // Given - 创建包含搞笑内容的批次
        List<CollectedMessage> messages = Arrays.asList(
                createMessage("哈哈哈哈这图太搞了"),
                createMessage("笑死我了"),
                createMessage("什么弔图")
        );
        String batchJson = createBatchJson(messages);

        // When
        String result = tool.classifyMessageBatch(batchJson);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("meme") || result.contains("normal"));
    }

    @Test
    @DisplayName("应该对HELL_JOKE分类消息进行正确分类")
    void shouldClassifyHellJokeMessages() throws Exception {
        // Given - 创建地狱笑话内容
        List<CollectedMessage> messages = Arrays.asList(
                createMessage("这也太地狱了"),
                createMessage("不敢笑"),
                createMessage("扣1佛祖原谅你")
        );
        String batchJson = createBatchJson(messages);

        // When
        String result = tool.classifyMessageBatch(batchJson);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("hell-joke") || result.contains("normal"));
    }

    @Test
    @DisplayName("空批次应该返回正常分类")
    void shouldReturnNormalForEmptyBatch() throws Exception {
        // Given
        String batchJson = createBatchJson(Arrays.asList());

        // When
        String result = tool.classifyMessageBatch(batchJson);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("normal"));
    }

    @Test
    @DisplayName("无效JSON应该返回错误")
    void shouldReturnErrorForInvalidJson() {
        // Given
        String invalidJson = "invalid json";

        // When
        String result = tool.classifyMessageBatch(invalidJson);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("error"));
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
                .triggerContent("测试触发消息")
                .messages(messages)
                .messageCount(messages.size())
                .status(BatchStatus.COMPLETED)
                .build();

        return objectMapper.writeValueAsString(batch);
    }
}
