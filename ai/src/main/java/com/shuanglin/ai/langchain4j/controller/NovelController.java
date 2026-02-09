package com.shuanglin.ai.langchain4j.controller;

import cn.hutool.core.util.IdUtil;
import com.shuanglin.ai.langchain4j.controller.dto.IsolationMetadata;
import com.shuanglin.ai.langchain4j.controller.dto.NovelRequest;
import com.shuanglin.ai.langchain4j.controller.dto.NovelResponse;
import com.shuanglin.ai.service.EntityStandardizer;
import com.shuanglin.ai.service.GraphService;
import com.shuanglin.dao.Articles.ArticlesEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 小说知识图谱控制器
 * 提供小说知识图谱构建的 REST API 接口
 */
@Slf4j
@RestController
@RequestMapping("/api/ai/novel")
public class NovelController {

    @Autowired
    private GraphService graphService;

    @Autowired
    private EntityStandardizer entityStandardizer;

    // ==================== GraphService API ====================

    /**
     * 构建小说知识图谱（增量扫描）
     * @param request 请求参数
     * @return 处理结果
     */
    @PostMapping("/graph/build")
    public NovelResponse buildGraph(@RequestBody NovelRequest request) {
        log.info("收到知识图谱构建请求，文件路径: {}", request.getFilePath());

        try {
            // 验证请求参数
            if (request.getFilePath() == null || request.getFilePath().isEmpty()) {
                return NovelResponse.fail("文件路径不能为空");
            }

            // 设置默认值
            if (request.getDataSource() == null) {
                request.setDataSource("novel_" + IdUtil.getSnowflakeNextIdStr());
            }

            // 构建元数据
            IsolationMetadata metadata = new IsolationMetadata();
            metadata.setBookUuid(request.getBookUuid() != null ? request.getBookUuid() : IdUtil.getSnowflakeNextIdStr());
            metadata.setBookName(request.getBookName() != null ? request.getBookName() : "未命名小说");
            metadata.setDataSource(request.getDataSource());

            // 调用服务
            if (request.getChapterLimit() != null && request.getChapterLimit() > 0) {
                // 限制章节数
                graphService.readStoryWithLimit(request.getFilePath(), request.getChapterLimit(), metadata);
            } else {
                // 完整构建
                graphService.readStory(request.getFilePath());
            }

            return NovelResponse.success(metadata.getBookUuid(), "知识图谱构建完成");

        } catch (Exception e) {
            log.error("知识图谱构建失败", e);
            return NovelResponse.fail("构建失败: " + e.getMessage());
        }
    }

    /**
     * 重放指定章节的 Cypher
     * @param request 请求参数
     * @return 处理结果
     */
    @PostMapping("/graph/replay")
    public NovelResponse replayCypher(@RequestBody NovelRequest request) {
        log.info("收到 Cypher 重放请求，bookUuid: {}, chapterIndex: {}",
                request.getBookUuid(), request.getChapterIndex());

        try {
            if (request.getBookUuid() == null || request.getChapterIndex() == null) {
                return NovelResponse.fail("bookUuid 和 chapterIndex 不能为空");
            }

            boolean success = graphService.replayCypherFromMongo(
                    request.getBookUuid(),
                    request.getChapterIndex()
            );

            if (success) {
                return NovelResponse.success(request.getBookUuid(), "Cypher 重放成功");
            } else {
                return NovelResponse.fail("Cypher 重放失败");
            }

        } catch (Exception e) {
            log.error("Cypher 重放失败", e);
            return NovelResponse.fail("重放失败: " + e.getMessage());
        }
    }

    /**
     * 清理测试数据
     * @param dataSource 数据源标识
     * @return 清理报告
     */
    @DeleteMapping("/graph/cleanup")
    public NovelResponse cleanup(@RequestParam String dataSource) {
        log.info("收到数据清理请求，数据源: {}", dataSource);

        try {
            var report = graphService.cleanupTestData(dataSource);
            NovelResponse response = NovelResponse.success(dataSource, "数据清理完成");
            response.setMessage("清理完成: " + report);
            return response;

        } catch (Exception e) {
            log.error("数据清理失败", e);
            return NovelResponse.fail("清理失败: " + e.getMessage());
        }
    }

    /**
     * 查询数据统计
     * @param dataSource 数据源标识
     * @return 统计信息
     */
    @GetMapping("/graph/stats")
    public NovelResponse stats(@RequestParam String dataSource) {
        log.info("收到数据统计请求，数据源: {}", dataSource);

        try {
            String stats = graphService.queryTestDataStats(dataSource);
            NovelResponse response = NovelResponse.success(dataSource, "查询成功");
            response.setStats(stats);
            return response;

        } catch (Exception e) {
            log.error("数据统计失败", e);
            return NovelResponse.fail("统计失败: " + e.getMessage());
        }
    }

    // ==================== EntityStandardizer API ====================

    /**
     * 扫描并构建实体注册表
     * @param request 请求参数
     * @return 处理结果
     */
    @PostMapping("/entity/scan")
    public NovelResponse scanEntities(@RequestBody NovelRequest request) {
        log.info("收到实体扫描请求，文件路径: {}", request.getFilePath());

        try {
            if (request.getFilePath() == null || request.getFilePath().isEmpty()) {
                return NovelResponse.fail("文件路径不能为空");
            }

            entityStandardizer.scanAndBuildRegistry(request.getFilePath());

            return NovelResponse.success(null, "实体扫描完成");

        } catch (Exception e) {
            log.error("实体扫描失败", e);
            return NovelResponse.fail("扫描失败: " + e.getMessage());
        }
    }

    /**
     * 根据别名查询实体ID
     * @param alias 别名
     * @param bookUuid 书籍UUID
     * @return 实体ID
     */
    @GetMapping("/entity/resolve")
    public String resolveAlias(@RequestParam String alias, @RequestParam String bookUuid) {
        log.info("收到别名解析请求，alias: {}, bookUuid: {}", alias, bookUuid);
        return entityStandardizer.resolveAlias(alias, bookUuid);
    }

    /**
     * 根据实体ID查询实体
     * @param entityId 实体ID
     * @return 实体信息
     */
    @GetMapping("/entity/{entityId}")
    public Object getEntity(@PathVariable String entityId) {
        log.info("收到实体查询请求，entityId: {}", entityId);
        return entityStandardizer.getEntityById(entityId);
    }
}
