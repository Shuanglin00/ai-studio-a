package com.shuanglin.ai.test.novel;

import com.shuanglin.ai.novel.service.GraphRagService;
import com.shuanglin.ai.novel.service.NovelEntityService;
import com.shuanglin.dao.model.EntityRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 知识图谱集成测试
 * 测试实体扫描、知识图谱构建和查询功能
 */
@SpringBootTest
@DisplayName("知识图谱集成测试")
class NovelKnowledgeGraphIntegrationTest {

    @Autowired
    private NovelEntityService novelEntityService;

    @Autowired
    private GraphRagService graphRagService;

    private static final String TEST_BOOK_UUID = "test-integration-001";
    private static final String TEST_BOOK_NAME = "从姑获鸟开始";

    @Test
    @DisplayName("测试1: 扫描小说实体")
    void testScanNovelEntities() {
        // 使用测试用EPUB文件
        String epubPath = "D:/project/ai-studio/ai/src/main/resources/21869-从姑获鸟开始【搜笔趣阁www.sobqg.com】.epub";

        // 执行实体扫描
        var report = novelEntityService.scanFullNovel(epubPath, TEST_BOOK_UUID, TEST_BOOK_NAME);

        // 验证
        assertNotNull(report);
        assertEquals(TEST_BOOK_UUID, report.getBookUuid());
        assertTrue(report.getTotalChapters() > 0, "应该有章节数据");
        System.out.println("扫描报告: " + report);
    }

    @Test
    @DisplayName("测试2: 获取所有实体")
    void testGetAllEntities() {
        // 获取所有实体
        List<EntityRegistry> entities = novelEntityService.getAllEntities(TEST_BOOK_UUID);

        // 验证
        assertNotNull(entities);
        System.out.println("实体数量: " + entities.size());

        // 打印前10个实体
        entities.stream().limit(10).forEach(e -> {
            System.out.println("实体: " + e.getEntityId() + " - " + e.getStandardName() + " (" + e.getEntityType() + ")");
        });
    }

    @Test
    @DisplayName("测试3: 按类型获取实体")
    void testGetEntitiesByType() {
        // 获取角色实体
        List<EntityRegistry> characters = novelEntityService.getEntitiesByType(TEST_BOOK_UUID, "CHARACTER");
        assertNotNull(characters);
        System.out.println("角色数量: " + characters.size());

        // 获取地点实体
        List<EntityRegistry> locations = novelEntityService.getEntitiesByType(TEST_BOOK_UUID, "LOCATION");
        assertNotNull(locations);
        System.out.println("地点数量: " + locations.size());

        // 获取组织实体
        List<EntityRegistry> orgs = novelEntityService.getEntitiesByType(TEST_BOOK_UUID, "ORGANIZATION");
        assertNotNull(orgs);
        System.out.println("组织数量: " + orgs.size());
    }

    @Test
    @DisplayName("测试4: 检索实体")
    void testRetrieveEntities() {
        // 检索相关实体
        List<EntityRegistry> entities = novelEntityService.retrieveEntities(TEST_BOOK_UUID, "李", 10);
        assertNotNull(entities);
        System.out.println("检索到包含'李'的实体: " + entities.size());
        entities.forEach(e -> System.out.println("  - " + e.getStandardName()));
    }

    @Test
    @DisplayName("测试5: 获取实体注册表JSON")
    void testGetEntityRegistryJson() {
        // 获取实体注册表JSON
        String json = novelEntityService.getEntityRegistryJson(TEST_BOOK_UUID);
        assertNotNull(json);
        assertTrue(json.length() > 0);
        System.out.println("实体注册表JSON长度: " + json.length());
        System.out.println("前500字符: " + json.substring(0, Math.min(500, json.length())));
    }

    @Test
    @DisplayName("测试6: 测试GraphRagService-获取实体当前状态")
    void testGetEntityCurrentState() {
        // 测试获取实体状态
        var states = graphRagService.getEntityCurrentState(TEST_BOOK_UUID, "主角");
        assertNotNull(states);
        System.out.println("实体状态: " + states);
    }

    @Test
    @DisplayName("测试7: 测试GraphRagService-获取实体历史事件")
    void testGetEntityHistoryEvents() {
        // 测试获取历史事件
        var events = graphRagService.getEntityHistoryEvents(TEST_BOOK_UUID, "主角");
        assertNotNull(events);
        System.out.println("历史事件数量: " + events.size());
    }

    @Test
    @DisplayName("测试8: 测试GraphRagService-获取实体关系")
    void testGetEntityRelations() {
        // 测试获取实体关系
        var relations = graphRagService.getEntityRelations(TEST_BOOK_UUID, "主角", "某个地点");
        assertNotNull(relations);
        System.out.println("实体关系数量: " + relations.size());
    }
}
