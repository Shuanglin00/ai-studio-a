package com.shuanglin.ai.test.novel;

import com.shuanglin.ai.langchain4j.assistant.DecomposeAssistant;
import com.shuanglin.ai.novel.service.EntityVerificationService;
import com.shuanglin.ai.novel.service.NovelEntityService;
import com.shuanglin.dao.model.EntityRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * EntityVerificationService 单元测试
 * 测试实体验证服务的核心方法
 */
@DisplayName("EntityVerificationService 测试")
@ExtendWith(MockitoExtension.class)
class EntityVerificationServiceTest {

    @Mock
    private DecomposeAssistant decomposeAssistant;

    @Mock
    private NovelEntityService novelEntityService;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private EntityVerificationService entityVerificationService;

    @BeforeEach
    void setUp() {
        // 设置
    }

    @Test
    @DisplayName("测试验证报告创建")
    void testVerificationReportCreation() {
        // 创建验证报告
        EntityVerificationService.VerificationReport report =
                new EntityVerificationService.VerificationReport("book-001");

        // 验证
        assertEquals("book-001", report.getBookUuid());
        assertEquals(0, report.getTotalEntities());
        assertEquals(0, report.getValidEntities());
        assertEquals(0, report.getIssues());
    }

    @Test
    @DisplayName("测试修正记录创建")
    void testCorrectionCreation() {
        // 创建修正记录
        EntityVerificationService.Correction correction =
                new EntityVerificationService.Correction();

        correction.setEntityId("CHAR_001");
        correction.setOriginalName("萧炎");
        correction.setNewName("炎帝");
        correction.setNewType("Character");

        // 验证
        assertEquals("CHAR_001", correction.getEntityId());
        assertEquals("萧炎", correction.getOriginalName());
        assertEquals("炎帝", correction.getNewName());
        assertEquals("Character", correction.getNewType());
    }

    @Test
    @DisplayName("测试追踪报告创建")
    void testTrackingReportCreation() {
        // 创建追踪报告
        EntityVerificationService.EntityTrackingReport report =
                new EntityVerificationService.EntityTrackingReport("book-001");

        // 验证
        assertEquals("book-001", report.getBookUuid());
        assertEquals(0, report.getConsistentEntities());
        assertEquals(0, report.getInconsistentEntities());
    }

    @Test
    @DisplayName("测试自动修正结果创建")
    void testAutoCorrectResultCreation() {
        // 创建自动修正结果
        EntityVerificationService.AutoCorrectResult result =
                new EntityVerificationService.AutoCorrectResult("book-001");

        result.setTotalCorrections(10);
        result.incrementSuccessCount();
        result.incrementSuccessCount();
        result.incrementFailedCount();

        // 验证
        assertEquals("book-001", result.getBookUuid());
        assertEquals(10, result.getTotalCorrections());
        assertEquals(2, result.getSuccessCount());
        assertEquals(1, result.getFailedCount());
    }

    @Test
    @DisplayName("测试修正报告累加方法")
    void testReportIncrementMethods() {
        // 创建验证报告
        EntityVerificationService.VerificationReport report =
                new EntityVerificationService.VerificationReport("book-001");

        // 测试累加方法
        report.incrementValidEntities();
        report.incrementValidEntities();
        report.incrementIssues();
        report.incrementIssues();
        report.incrementIssues();

        // 验证
        assertEquals(2, report.getValidEntities());
        assertEquals(3, report.getIssues());
    }

    @Test
    @DisplayName("测试修正添加方法")
    void testCorrectionAddMethod() {
        // 创建验证报告
        EntityVerificationService.VerificationReport report =
                new EntityVerificationService.VerificationReport("book-001");

        // 添加修正
        EntityVerificationService.Correction correction = new EntityVerificationService.Correction();
        correction.setEntityId("CHAR_001");
        correction.setNewName("新名称");
        report.addCorrection(correction);

        // 验证
        assertEquals(1, report.getCorrections().size());
        assertEquals("CHAR_001", report.getCorrections().get(0).getEntityId());
    }

    @Test
    @DisplayName("测试服务初始化")
    void testServiceInitialization() {
        // 验证服务能够正常初始化
        assertNotNull(entityVerificationService);
    }
}
