package com.shuanglin.dao.classification;

import com.shuanglin.dao.classification.enums.ClassificationSource;
import com.shuanglin.dao.classification.enums.MessageCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 消息分类结果实体测试
 */
@DisplayName("消息分类结果实体测试")
class MessageClassificationTest {

    @Test
    @DisplayName("应该能使用Builder创建对象")
    void shouldCreateUsingBuilder() {
        // Given
        String batchId = "batch-001";
        String groupId = "123456789";
        Long triggerMessageId = 11111L;

        // When
        MessageClassification classification = MessageClassification.builder()
                .id("class-001")
                .batchId(batchId)
                .groupId(groupId)
                .triggerMessageId(triggerMessageId)
                .category(MessageCategory.MEME)
                .confidence(0.95)
                .reasoning("消息内容符合弔图特征")
                .source(ClassificationSource.AUTO)
                .classifiedAt(LocalDateTime.now())
                .build();

        // Then
        assertNotNull(classification);
        assertEquals("class-001", classification.getId());
        assertEquals(batchId, classification.getBatchId());
        assertEquals(groupId, classification.getGroupId());
        assertEquals(triggerMessageId, classification.getTriggerMessageId());
        assertEquals(MessageCategory.MEME, classification.getCategory());
        assertEquals(0.95, classification.getConfidence());
        assertEquals(ClassificationSource.AUTO, classification.getSource());
    }

    @Test
    @DisplayName("应该能设置子分类列表")
    void shouldSetSubCategories() {
        // Given
        List<MessageCategory> subCategories = Arrays.asList(
                MessageCategory.MEME,
                MessageCategory.NORMAL
        );

        // When
        MessageClassification classification = MessageClassification.builder()
                .id("class-001")
                .batchId("batch-001")
                .category(MessageCategory.MEME)
                .subCategories(subCategories)
                .build();

        // Then
        assertNotNull(classification.getSubCategories());
        assertEquals(2, classification.getSubCategories().size());
    }

    @Test
    @DisplayName("手动标注应该记录分类者")
    void manualClassificationShouldRecordUser() {
        // Given
        Long classifiedBy = 12345L;

        // When
        MessageClassification classification = MessageClassification.builder()
                .id("class-001")
                .batchId("batch-001")
                .category(MessageCategory.MEME)
                .source(ClassificationSource.MANUAL)
                .classifiedBy(classifiedBy)
                .classifiedAt(LocalDateTime.now())
                .build();

        // Then
        assertEquals(ClassificationSource.MANUAL, classification.getSource());
        assertEquals(classifiedBy, classification.getClassifiedBy());
    }

    @Test
    @DisplayName("自动分类的分类者应该为空")
    void autoClassificationShouldHaveNullClassifiedBy() {
        // When
        MessageClassification classification = MessageClassification.builder()
                .id("class-001")
                .batchId("batch-001")
                .category(MessageCategory.NORMAL)
                .source(ClassificationSource.AUTO)
                .classifiedAt(LocalDateTime.now())
                .build();

        // Then
        assertEquals(ClassificationSource.AUTO, classification.getSource());
        assertNull(classification.getClassifiedBy());
    }

    @Test
    @DisplayName("应该支持无参构造")
    void shouldSupportNoArgsConstructor() {
        // When
        MessageClassification classification = new MessageClassification();

        // Then
        assertNotNull(classification);
    }

    @Test
    @DisplayName("置信度应该在有效范围内")
    void confidenceShouldBeInValidRange() {
        // Given
        double highConfidence = 0.95;
        double lowConfidence = 0.30;

        // When
        MessageClassification high = MessageClassification.builder()
                .confidence(highConfidence)
                .build();
        MessageClassification low = MessageClassification.builder()
                .confidence(lowConfidence)
                .build();

        // Then
        assertEquals(highConfidence, high.getConfidence());
        assertEquals(lowConfidence, low.getConfidence());
    }
}
