package com.shuanglin.ai.novel.controller;

import com.shuanglin.ai.novel.service.*;
import com.shuanglin.ai.novel.service.EntityVerificationService.VerificationReport;
import com.shuanglin.ai.novel.service.EventVerificationService.EventVerificationReport;
import com.shuanglin.ai.novel.service.EventVerificationService.TemporalVerificationReport;
import com.shuanglin.ai.novel.service.GenerationPrompt.ChapterPrompt;
import com.shuanglin.ai.novel.service.GenerationPrompt.GenerationContext;
import com.shuanglin.ai.novel.service.NovelEntityService.EntityScanReport;
import com.shuanglin.ai.novel.service.GraphRagService.EventProcessReport;
import com.shuanglin.ai.novel.service.NovelGenerationService.CharacterStateInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 小说知识图谱交互控制器
 * 提供实体扫描、事件处理、验证和章节生成的REST API
 */
@Slf4j
@RestController
@RequestMapping("/api/novel")
public class NovelChatController {

    @Autowired
    private NovelEntityService novelEntityService;

    @Autowired
    private GraphRagService graphRagService;

    @Autowired
    private EntityVerificationService entityVerificationService;

    @Autowired
    private EventVerificationService eventVerificationService;

    @Autowired
    private NovelGenerationService novelGenerationService;

    // ==================== 阶段一：实体扫描 API ====================

    /**
     * 扫描全书实体（阶段一）
     */
    @PostMapping("/{bookId}/entities/scan")
    public ApiResponse<EntityScanReport> scanEntities(
            @PathVariable String bookId,
            @RequestBody ScanEntitiesRequest request) {
        log.info("收到实体扫描请求，书籍ID: {}, 路径: {}", bookId, request.getEpubPath());

        try {
            EntityScanReport report = novelEntityService.scanFullNovel(
                    request.getEpubPath(),
                    bookId,
                    request.getBookName()
            );
            return ApiResponse.success(report);
        } catch (Exception e) {
            log.error("实体扫描失败", e);
            return ApiResponse.fail("实体扫描失败: " + e.getMessage());
        }
    }

    /**
     * 获取书籍所有实体
     */
    @GetMapping("/{bookId}/entities")
    public ApiResponse<?> getEntities(@PathVariable String bookId) {
        try {
            var entities = novelEntityService.getAllEntities(bookId);
            return ApiResponse.success(entities);
        } catch (Exception e) {
            log.error("获取实体列表失败", e);
            return ApiResponse.fail("获取实体列表失败: " + e.getMessage());
        }
    }

    /**
     * 按类型获取实体
     */
    @GetMapping("/{bookId}/entities/{entityType}")
    public ApiResponse<?> getEntitiesByType(
            @PathVariable String bookId,
            @PathVariable String entityType) {
        try {
            var entities = novelEntityService.getEntitiesByType(bookId, entityType);
            return ApiResponse.success(entities);
        } catch (Exception e) {
            log.error("获取实体列表失败", e);
            return ApiResponse.fail("获取实体列表失败: " + e.getMessage());
        }
    }

    // ==================== 阶段二：事件处理 API ====================

    /**
     * 处理全书事件（阶段二）
     */
    @PostMapping("/{bookId}/events/process")
    public ApiResponse<EventProcessReport> processEvents(
            @PathVariable String bookId,
            @RequestBody ProcessEventsRequest request) {
        log.info("收到事件处理请求，书籍ID: {}", bookId);

        try {
            EventProcessReport report = graphRagService.processAllChapters(
                    bookId,
                    request.getEpubPath()
            );
            return ApiResponse.success(report);
        } catch (Exception e) {
            log.error("事件处理失败", e);
            return ApiResponse.fail("事件处理失败: " + e.getMessage());
        }
    }

    /**
     * 获取实体当前状态
     */
    @GetMapping("/{bookId}/character/{name}/state")
    public ApiResponse<?> getCharacterState(
            @PathVariable String bookId,
            @PathVariable String name) {
        try {
            var state = graphRagService.getEntityCurrentState(bookId, name);
            return ApiResponse.success(state);
        } catch (Exception e) {
            log.error("获取实体状态失败", e);
            return ApiResponse.fail("获取实体状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取实体历史事件
     */
    @GetMapping("/{bookId}/character/{name}/history")
    public ApiResponse<?> getCharacterHistory(
            @PathVariable String bookId,
            @PathVariable String name) {
        try {
            var history = graphRagService.getEntityHistoryEvents(bookId, name);
            return ApiResponse.success(history);
        } catch (Exception e) {
            log.error("获取历史事件失败", e);
            return ApiResponse.fail("获取历史事件失败: " + e.getMessage());
        }
    }

    /**
     * 获取两个实体的关系
     */
    @GetMapping("/{bookId}/relation")
    public ApiResponse<?> getRelation(
            @PathVariable String bookId,
            @RequestParam String entity1,
            @RequestParam String entity2) {
        try {
            var relations = graphRagService.getEntityRelations(bookId, entity1, entity2);
            return ApiResponse.success(relations);
        } catch (Exception e) {
            log.error("获取实体关系失败", e);
            return ApiResponse.fail("获取实体关系失败: " + e.getMessage());
        }
    }

    // ==================== 阶段三：验证 API ====================

    /**
     * 验证实体一致性
     */
    @PostMapping("/{bookId}/entities/verify")
    public ApiResponse<VerificationReport> verifyEntities(@PathVariable String bookId) {
        log.info("收到实体验证请求，书籍ID: {}", bookId);

        try {
            VerificationReport report = entityVerificationService.verifyEntityConsistency(bookId);
            return ApiResponse.success(report);
        } catch (Exception e) {
            log.error("实体验证失败", e);
            return ApiResponse.fail("实体验证失败: " + e.getMessage());
        }
    }

    /**
     * 自动修正实体
     */
    @PostMapping("/{bookId}/entities/auto-correct")
    public ApiResponse<?> autoCorrectEntities(@PathVariable String bookId) {
        log.info("收到自动修正请求，书籍ID: {}", bookId);

        try {
            // 先验证再修正
            VerificationReport report = entityVerificationService.verifyEntityConsistency(bookId);
            var result = entityVerificationService.autoCorrect(report);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("自动修正失败", e);
            return ApiResponse.fail("自动修正失败: " + e.getMessage());
        }
    }

    /**
     * 追踪跨章节实体
     */
    @PostMapping("/{bookId}/entities/track")
    public ApiResponse<?> trackEntities(@PathVariable String bookId) {
        log.info("收到实体追踪请求，书籍ID: {}", bookId);

        try {
            var report = entityVerificationService.trackEntitiesAcrossChapters(bookId);
            return ApiResponse.success(report);
        } catch (Exception e) {
            log.error("实体追踪失败", e);
            return ApiResponse.fail("实体追踪失败: " + e.getMessage());
        }
    }

    /**
     * 验证事件-实体关联
     */
    @PostMapping("/{bookId}/events/verify")
    public ApiResponse<EventVerificationReport> verifyEvents(@PathVariable String bookId) {
        log.info("收到事件验证请求，书籍ID: {}", bookId);

        try {
            EventVerificationReport report = eventVerificationService.verifyEventEntityRelations(bookId);
            return ApiResponse.success(report);
        } catch (Exception e) {
            log.error("事件验证失败", e);
            return ApiResponse.fail("事件验证失败: " + e.getMessage());
        }
    }

    /**
     * 验证事件时序一致性
     */
    @PostMapping("/{bookId}/events/verify-temporal")
    public ApiResponse<TemporalVerificationReport> verifyTemporal(@PathVariable String bookId) {
        log.info("收到时序验证请求，书籍ID: {}", bookId);

        try {
            TemporalVerificationReport report = eventVerificationService.verifyTemporalConsistency(bookId);
            return ApiResponse.success(report);
        } catch (Exception e) {
            log.error("时序验证失败", e);
            return ApiResponse.fail("时序验证失败: " + e.getMessage());
        }
    }

    // ==================== 阶段四：生成 API ====================

    /**
     * 生成新章节（续写模式）
     */
    @PostMapping("/{bookId}/generate/continue")
    public ApiResponse<String> generateContinue(
            @PathVariable String bookId,
            @RequestBody GenerateRequest request) {
        log.info("收到续写请求，书籍ID: {}", bookId);

        try {
            ChapterPrompt prompt = new ChapterPrompt();
            prompt.setRecentChaptersSummary(request.getRecentSummary());
            prompt.setChapterTitle(request.getChapterTitle());
            prompt.setChapterLength(request.getChapterLength() > 0 ? request.getChapterLength() : 3000);

            String content = novelGenerationService.continueStory(bookId, prompt);
            return ApiResponse.success(content);
        } catch (Exception e) {
            log.error("续写失败", e);
            return ApiResponse.fail("续写失败: " + e.getMessage());
        }
    }

    /**
     * 生成新章节（概要模式）
     */
    @PostMapping("/{bookId}/generate/outline")
    public ApiResponse<String> generateFromOutline(
            @PathVariable String bookId,
            @RequestBody GenerateOutlineRequest request) {
        log.info("收到概要生成请求，书籍ID: {}, 概要: {}", bookId, request.getOutline());

        try {
            ChapterPrompt prompt = new ChapterPrompt();
            prompt.setChapterLength(request.getChapterLength() > 0 ? request.getChapterLength() : 3000);

            String content = novelGenerationService.generateFromOutline(
                    bookId,
                    request.getOutline(),
                    prompt
            );
            return ApiResponse.success(content);
        } catch (Exception e) {
            log.error("概要生成失败", e);
            return ApiResponse.fail("概要生成失败: " + e.getMessage());
        }
    }

    /**
     * 生成新章节（混合模式）
     */
    @PostMapping("/{bookId}/generate/hybrid")
    public ApiResponse<String> generateHybrid(
            @PathVariable String bookId,
            @RequestBody GenerateHybridRequest request) {
        log.info("收到混合生成请求，书籍ID: {}", bookId);

        try {
            ChapterPrompt prompt = new ChapterPrompt();
            prompt.setChapterLength(request.getChapterLength() > 0 ? request.getChapterLength() : 3000);

            String content = novelGenerationService.generateHybrid(
                    bookId,
                    request.getContent(),
                    request.getOutline(),
                    prompt
            );
            return ApiResponse.success(content);
        } catch (Exception e) {
            log.error("混合生成失败", e);
            return ApiResponse.fail("混合生成失败: " + e.getMessage());
        }
    }

    /**
     * 交互式问答
     */
    @PostMapping("/{bookId}/chat")
    public ApiResponse<String> chat(
            @PathVariable String bookId,
            @RequestBody ChatRequest request) {
        log.info("收到问答请求，书籍ID: {}, 问题: {}", bookId, request.getQuestion());

        try {
            String answer = novelGenerationService.chat(bookId, request.getQuestion());
            return ApiResponse.success(answer);
        } catch (Exception e) {
            log.error("问答失败", e);
            return ApiResponse.fail("问答失败: " + e.getMessage());
        }
    }

    /**
     * 查询人物状态
     */
    @GetMapping("/{bookId}/character/{name}/info")
    public ApiResponse<CharacterStateInfo> getCharacterInfo(
            @PathVariable String bookId,
            @PathVariable String name) {
        try {
            CharacterStateInfo info = novelGenerationService.getCharacterState(bookId, name);
            return ApiResponse.success(info);
        } catch (Exception e) {
            log.error("查询人物状态失败", e);
            return ApiResponse.fail("查询人物状态失败: " + e.getMessage());
        }
    }

    // ==================== 请求/响应类 ====================

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScanEntitiesRequest {
        private String epubPath;
        private String bookName;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProcessEventsRequest {
        private String epubPath;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenerateRequest {
        private String recentSummary;
        private String chapterTitle;
        private int chapterLength;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenerateOutlineRequest {
        private String outline;
        private int chapterLength;
    }

    @Data
    public static class GenerateHybridRequest {
        private String content;
        private String outline;
        private int chapterLength;
    }

    @Data
    public static class ChatRequest {
        private String question;
    }

    /**
     * 统一API响应
     */
    @Data
    public static class ApiResponse<T> {
        private boolean success;
        private T data;
        private String error;

        public static <T> ApiResponse<T> success(T data) {
            ApiResponse<T> response = new ApiResponse<>();
            response.setSuccess(true);
            response.setData(data);
            return response;
        }

        public static <T> ApiResponse<T> fail(String error) {
            ApiResponse<T> response = new ApiResponse<>();
            response.setSuccess(false);
            response.setError(error);
            return response;
        }
    }
}
