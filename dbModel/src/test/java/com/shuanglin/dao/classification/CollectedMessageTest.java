package com.shuanglin.dao.classification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 收集的消息项测试
 */
@DisplayName("收集的消息项测试")
class CollectedMessageTest {

    @Test
    @DisplayName("应该能使用Builder创建对象")
    void shouldCreateUsingBuilder() {
        // Given
        Long messageId = 12345L;
        Long userId = 67890L;
        String nickname = "测试用户";
        String content = "测试消息内容";

        // When
        CollectedMessage message = CollectedMessage.builder()
                .messageId(messageId)
                .userId(userId)
                .nickname(nickname)
                .content(content)
                .timestamp(System.currentTimeMillis())
                .build();

        // Then
        assertNotNull(message);
        assertEquals(messageId, message.getMessageId());
        assertEquals(userId, message.getUserId());
        assertEquals(nickname, message.getNickname());
        assertEquals(content, message.getContent());
    }

    @Test
    @DisplayName("应该能正确设置和获取回复消息属性")
    void shouldHandleReplyMessage() {
        // Given
        Long replyToMessageId = 11111L;

        // When
        CollectedMessage message = CollectedMessage.builder()
                .messageId(12345L)
                .userId(67890L)
                .content("回复消息")
                .isReply(true)
                .replyToMessageId(replyToMessageId)
                .build();

        // Then
        assertTrue(message.getIsReply());
        assertEquals(replyToMessageId, message.getReplyToMessageId());
    }

    @Test
    @DisplayName("应该能正确设置消息段列表")
    void shouldHandleMessageSegments() {
        // Given
        List<MessageSegment> segments = Arrays.asList(
                MessageSegment.text("Hello "),
                MessageSegment.at(12345L)
        );

        // When
        CollectedMessage message = CollectedMessage.builder()
                .messageId(12345L)
                .userId(67890L)
                .content("Hello @user")
                .segments(segments)
                .build();

        // Then
        assertNotNull(message.getSegments());
        assertEquals(2, message.getSegments().size());
    }

    @Test
    @DisplayName("应该支持无参构造")
    void shouldSupportNoArgsConstructor() {
        // When
        CollectedMessage message = new CollectedMessage();

        // Then
        assertNotNull(message);
    }

    @Test
    @DisplayName("应该支持全参构造")
    void shouldSupportAllArgsConstructor() {
        // Given
        Long timestamp = System.currentTimeMillis();
        List<MessageSegment> segments = Arrays.asList(MessageSegment.text("test"));

        // When
        CollectedMessage message = new CollectedMessage(
                12345L, 67890L, "昵称", "内容",
                segments, timestamp, true, 11111L
        );

        // Then
        assertEquals(12345L, message.getMessageId());
        assertEquals(67890L, message.getUserId());
        assertEquals("昵称", message.getNickname());
        assertEquals("内容", message.getContent());
        assertEquals(segments, message.getSegments());
        assertEquals(timestamp, message.getTimestamp());
        assertTrue(message.getIsReply());
        assertEquals(11111L, message.getReplyToMessageId());
    }
}
