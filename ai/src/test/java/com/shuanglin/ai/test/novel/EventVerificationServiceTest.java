package com.shuanglin.ai.test.novel;

import com.shuanglin.ai.langchain4j.assistant.DecomposeAssistant;
import com.shuanglin.ai.novel.service.EventVerificationService;
import com.shuanglin.ai.novel.service.NovelEntityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.neo4j.driver.Driver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EventVerificationService 单元测试
 * 测试事件验证服务的核心方法
 */
@DisplayName("EventVerificationService 测试")
@ExtendWith(MockitoExtension.class)
class EventVerificationServiceTest {

    @Mock
    private DecomposeAssistant decomposeAssistant;

    @Mock
    private NovelEntityService novelEntityService;

    @Mock
    private Driver driver;

    @InjectMocks
    private EventVerificationService eventVerificationService;

    @BeforeEach
    void setUp() {
        // 设置
    }

    @Test
    @DisplayName("测试事件验证报告创建")
    void testEventVerificationReportCreation() {
        // 创建事件验证报告
        EventVerificationService.EventVerificationReport report =
                new EventVerificationService.EventVerificationReport("book-001");

        // 验证
        assertEquals("book-001", report.getBookUuid());
        assertEquals(0, report.getTotalEvents());
        assertEquals(0, report.getValidEvents());
        assertEquals(0, report.getInvalidEvents());
    }

    @Test
    @DisplayName("测试时序验证报告创建")
    void testTemporalVerificationReportCreation() {
        // 创建时序验证报告
        EventVerificationService.TemporalVerificationReport report =
                new EventVerificationService.TemporalVerificationReport("book-001");

        // 验证
        assertEquals("book-001", report.getBookUuid());
        assertEquals(0, report.getTotalEvents());
        assertEquals(0, report.getIssues());
    }

    @Test
    @DisplayName("测试因果验证报告创建")
    void testCausalityVerificationReportCreation() {
        // 创建因果验证报告
        EventVerificationService.CausalityVerificationReport report =
                new EventVerificationService.CausalityVerificationReport("book-001");

        // 验证
        assertEquals("book-001", report.getBookUuid());
        assertTrue(report.getMissingPredecessors().isEmpty());
        assertTrue(report.getWeakCausalities().isEmpty());
    }

    @Test
    @DisplayName("测试时序问题创建")
    void testTemporalIssueCreation() {
        // 创建时序问题
        EventVerificationService.TemporalIssue issue = new EventVerificationService.TemporalIssue();
        issue.setEntity("主角");
        issue.setEventChapter(5);
        issue.setLastChapter(3);
        issue.setIssueType("STATE_REGRESSION");
        issue.setDescription("实体 主角 在第5章发生状态变化");

        // 验证
        assertEquals("主角", issue.getEntity());
        assertEquals(5, issue.getEventChapter());
        assertEquals(3, issue.getLastChapter());
        assertEquals("STATE_REGRESSION", issue.getIssueType());
    }

    @Test
    @DisplayName("测试事件验证报告累加方法")
    void testEventReportIncrementMethods() {
        // 创建事件验证报告
        EventVerificationService.EventVerificationReport report =
                new EventVerificationService.EventVerificationReport("book-001");

        // 测试累加方法
        report.incrementValidEvents();
        report.incrementValidEvents();
        report.incrementInvalidEvents();

        // 验证
        assertEquals(2, report.getValidEvents());
        assertEquals(1, report.getInvalidEvents());
    }

    @Test
    @DisplayName("测试时序验证报告累加方法")
    void testTemporalReportIncrementMethods() {
        // 创建时序验证报告
        EventVerificationService.TemporalVerificationReport report =
                new EventVerificationService.TemporalVerificationReport("book-001");

        // 添加时序问题
        EventVerificationService.TemporalIssue issue = new EventVerificationService.TemporalIssue();
        issue.setEntity("主角");
        report.addIssue(issue);

        report.incrementIssues();

        // 验证
        assertEquals(1, report.getIssues());
        assertEquals(1, report.getTemporalIssues().size());
    }

    @Test
    @DisplayName("测试因果验证报告添加方法")
    void testCausalityReportAddMethods() {
        // 创建因果验证报告
        EventVerificationService.CausalityVerificationReport report =
                new EventVerificationService.CausalityVerificationReport("book-001");

        // 添加缺失前置事件
        Map<String, Object> event = new HashMap<>();
        event.put("chapterIndex", 5);
        event.put("eventType", "BREAKTHROUGH");
        report.addMissingPredecessor(event, "CULTIVATION");

        // 添加弱因果关系
        Map<String, Object> event2 = new HashMap<>();
        event2.put("chapterIndex", 3);
        event2.put("eventType", "BATTLE");
        report.addWeakCausality(event2);

        // 验证
        assertEquals(1, report.getMissingPredecessors().size());
        assertEquals(1, report.getWeakCausalities().size());
    }

    @Test
    @DisplayName("测试事件验证-添加问题")
    void testEventVerificationAddIssue() {
        // 创建事件验证报告
        EventVerificationService.EventVerificationReport report =
                new EventVerificationService.EventVerificationReport("book-001");

        // 添加问题
        List<String> issues = new ArrayList<>();
        issues.add("实体引用无效");
        issues.add("事件类型错误");
        report.addEventIssue("evt-001", issues);

        // 验证
        assertEquals(1, report.getEventIssues().size());
        assertTrue(report.getEventIssues().containsKey("evt-001"));
    }

    @Test
    @DisplayName("测试服务初始化")
    void testServiceInitialization() {
        // 验证服务能够正常初始化
        assertNotNull(eventVerificationService);
    }
}
