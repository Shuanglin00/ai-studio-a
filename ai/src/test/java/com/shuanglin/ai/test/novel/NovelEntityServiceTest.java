package com.shuanglin.ai.test.novel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shuanglin.ai.langchain4j.assistant.DecomposeAssistant;
import com.shuanglin.ai.novel.service.NovelEntityService;
import com.shuanglin.dao.model.EntityMention;
import com.shuanglin.dao.model.EntityRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * NovelEntityService 单元测试
 * 测试实体扫描服务的核心方法
 */
@DisplayName("NovelEntityService 测试")
@ExtendWith(MockitoExtension.class)
class NovelEntityServiceTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private DecomposeAssistant decomposeAssistant;

    @InjectMocks
    private NovelEntityService novelEntityService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        // 可以在这里添加额外的设置
    }

    @Test
    @DisplayName("测试获取所有实体")
    void testGetAllEntities() {
        // 准备Mock数据
        List<EntityRegistry> mockEntities = new ArrayList<>();
        EntityRegistry entity = new EntityRegistry();
        entity.setEntityId("CHAR_001");
        entity.setStandardName("主角");
        entity.setEntityType("Character");
        entity.setBookUuid("book-001");
        mockEntities.add(entity);

        when(mongoTemplate.find(any(Query.class), eq(EntityRegistry.class), eq("entity_registry")))
                .thenReturn(mockEntities);

        // 执行
        List<EntityRegistry> result = novelEntityService.getAllEntities("book-001");

        // 验证
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("主角", result.get(0).getStandardName());
        verify(mongoTemplate, times(1)).find(any(Query.class), eq(EntityRegistry.class), eq("entity_registry"));
    }

    @Test
    @DisplayName("测试按类型获取实体")
    void testGetEntitiesByType() {
        // 准备Mock数据
        List<EntityRegistry> mockEntities = new ArrayList<>();
        EntityRegistry entity = new EntityRegistry();
        entity.setEntityId("CHAR_001");
        entity.setStandardName("主角");
        entity.setEntityType("Character");
        entity.setBookUuid("book-001");
        mockEntities.add(entity);

        when(mongoTemplate.find(any(Query.class), eq(EntityRegistry.class), eq("entity_registry")))
                .thenReturn(mockEntities);

        // 执行
        List<EntityRegistry> result = novelEntityService.getEntitiesByType("book-001", "Character");

        // 验证
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Character", result.get(0).getEntityType());
    }

    @Test
    @DisplayName("测试获取实体注册表JSON")
    void testGetEntityRegistryJson() {
        // 准备Mock数据
        List<EntityRegistry> mockEntities = new ArrayList<>();
        EntityRegistry entity = new EntityRegistry();
        entity.setEntityId("CHAR_001");
        entity.setStandardName("主角");
        entity.setEntityType("Character");
        entity.setBookUuid("book-001");

        List<String> aliases = new ArrayList<>();
        aliases.add("主人公");
        entity.setAliases(aliases);

        mockEntities.add(entity);

        when(mongoTemplate.find(any(Query.class), eq(EntityRegistry.class), eq("entity_registry")))
                .thenReturn(mockEntities);

        // 执行
        String result = novelEntityService.getEntityRegistryJson("book-001");

        // 验证
        assertNotNull(result);
        assertTrue(result.contains("CHAR_001"));
        assertTrue(result.contains("主角"));
        assertTrue(result.contains("Character"));
    }

    @Test
    @DisplayName("测试检索实体-基本查询")
    void testRetrieveEntities() {
        // 准备Mock数据
        List<EntityRegistry> mockEntities = new ArrayList<>();
        EntityRegistry entity = new EntityRegistry();
        entity.setEntityId("CHAR_001");
        entity.setStandardName("萧炎");
        entity.setEntityType("Character");
        entity.setBookUuid("book-001");
        entity.setMentionCount(10);

        List<String> aliases = new ArrayList<>();
        aliases.add("炎帝");
        entity.setAliases(aliases);

        mockEntities.add(entity);

        when(mongoTemplate.find(any(Query.class), eq(EntityRegistry.class), eq("entity_registry")))
                .thenReturn(mockEntities);

        // 执行
        List<EntityRegistry> result = novelEntityService.retrieveEntities("book-001", "萧炎", 5);

        // 验证
        assertNotNull(result);
    }

    @Test
    @DisplayName("测试获取实体注册表JSON-空数据")
    void testGetEntityRegistryJsonEmpty() {
        // 准备Mock数据 - 空列表
        when(mongoTemplate.find(any(Query.class), eq(EntityRegistry.class), eq("entity_registry")))
                .thenReturn(new ArrayList<>());

        // 执行
        String result = novelEntityService.getEntityRegistryJson("book-001");

        // 验证
        assertNotNull(result);
        assertEquals("[]", result);
    }

    @Test
    @DisplayName("测试EntityMention创建")
    void testEntityMentionCreation() {
        // 测试EntityMention的创建
        EntityMention mention = new EntityMention();
        mention.setStandardName("主角");
        mention.setType("Character");

        List<String> aliases = new ArrayList<>();
        aliases.add("主人公");
        aliases.add("萧炎");
        mention.setAliases(aliases);

        // 验证
        assertEquals("主角", mention.getStandardName());
        assertEquals("Character", mention.getType());
        assertEquals(2, mention.getAliases().size());
        assertTrue(mention.getAllNames().contains("主角"));
        assertTrue(mention.getAllNames().contains("主人公"));
    }

    @Test
    @DisplayName("测试EntityRegistry数据封装")
    void testEntityRegistryData() {
        // 测试EntityRegistry的数据封装
        EntityRegistry registry = new EntityRegistry();
        registry.setEntityId("CHAR_001");
        registry.setStandardName("主角");
        registry.setEntityType("Character");
        registry.setBookUuid("book-001");
        registry.setFirstMentionChapter(1);
        registry.setConfidence(0.95);

        // 测试别名添加
        registry.addAlias("主人公");
        registry.addAlias("萧炎");
        registry.addAlias("主角"); // 重复添加，应该被忽略

        // 验证
        assertEquals("CHAR_001", registry.getEntityId());
        assertEquals("主角", registry.getStandardName());
        assertEquals(2, registry.getAliases().size()); // 不包含重复的"主角"

        // 测试所有名称
        List<String> allNames = registry.getAllNames();
        assertTrue(allNames.contains("主角"));
        assertTrue(allNames.contains("主人公"));
        assertTrue(allNames.contains("萧炎"));
    }

    @Test
    @DisplayName("测试章节提及记录")
    void testChapterMention() {
        // 测试章节提及记录功能
        EntityRegistry registry = new EntityRegistry();
        registry.setEntityId("CHAR_001");

        // 添加章节提及
        registry.addChapterMention(1);
        registry.addChapterMention(5);
        registry.addChapterMention(10);
        registry.addChapterMention(10); // 重复，应该被忽略

        // 验证
        assertEquals(3, registry.getMentionChapters().size());
        assertEquals(3, registry.getMentionCount());
        assertTrue(registry.getMentionChapters().contains(1));
        assertTrue(registry.getMentionChapters().contains(5));
        assertTrue(registry.getMentionChapters().contains(10));
    }
}
