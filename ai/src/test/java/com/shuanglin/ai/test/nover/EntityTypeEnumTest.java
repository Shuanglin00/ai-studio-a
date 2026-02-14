package com.shuanglin.ai.test.novel;

import com.shuanglin.ai.novel.enums.EntityTypeEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EntityTypeEnum 枚举测试
 */
@DisplayName("EntityTypeEnum 枚举测试")
class EntityTypeEnumTest {

    @Test
    @DisplayName("测试枚举值数量")
    void testEnumValues() {
        EntityTypeEnum[] types = EntityTypeEnum.values();
        assertEquals(20, types.length, "应该有20种实体类型");
    }

    @Test
    @DisplayName("测试从Label获取枚举")
    void testFromLabel() {
        assertEquals(EntityTypeEnum.CHARACTER, EntityTypeEnum.fromLabel("Character"));
        assertEquals(EntityTypeEnum.LOCATION, EntityTypeEnum.fromLabel("Location"));
        assertEquals(EntityTypeEnum.ORGANIZATION, EntityTypeEnum.fromLabel("Organization"));
        assertEquals(EntityTypeEnum.ITEM, EntityTypeEnum.fromLabel("Item"));
        assertEquals(EntityTypeEnum.SKILL, EntityTypeEnum.fromLabel("Skill"));
        assertEquals(EntityTypeEnum.REALM, EntityTypeEnum.fromLabel("Realm"));
    }

    @Test
    @DisplayName("测试大小写不敏感")
    void testCaseInsensitive() {
        assertEquals(EntityTypeEnum.CHARACTER, EntityTypeEnum.fromLabel("CHARACTER"));
        assertEquals(EntityTypeEnum.CHARACTER, EntityTypeEnum.fromLabel("character"));
        assertEquals(EntityTypeEnum.CHARACTER, EntityTypeEnum.fromLabel("Character"));
    }

    @Test
    @DisplayName("测试无效Label返回null")
    void testInvalidLabel() {
        assertNull(EntityTypeEnum.fromLabel("InvalidType"));
        assertNull(EntityTypeEnum.fromLabel(""));
        assertNull(EntityTypeEnum.fromLabel(null));
    }

    @Test
    @DisplayName("测试枚举属性")
    void testEnumProperties() {
        // 测试 CHARACTER
        EntityTypeEnum character = EntityTypeEnum.CHARACTER;
        assertEquals("Character", character.getLabel());
        assertEquals("角色", character.getName());
        assertTrue(character.getDescription().contains("人名"));

        // 测试 LOCATION
        EntityTypeEnum location = EntityTypeEnum.LOCATION;
        assertEquals("Location", location.getLabel());
        assertEquals("地点/场景", location.getName());

        // 测试 ORGANIZATION
        EntityTypeEnum org = EntityTypeEnum.ORGANIZATION;
        assertEquals("Organization", org.getLabel());
        assertEquals("组织/势力", org.getName());

        // 测试 ITEM
        EntityTypeEnum item = EntityTypeEnum.ITEM;
        assertEquals("Item", item.getLabel());
        assertEquals("物品/法宝", item.getName());

        // 测试 SKILL
        EntityTypeEnum skill = EntityTypeEnum.SKILL;
        assertEquals("Skill", skill.getLabel());
        assertEquals("技能/功法", skill.getName());
    }

    @Test
    @DisplayName("测试新增类型")
    void testNewTypes() {
        // 测试新增的类型
        assertNotNull(EntityTypeEnum.SPECIES);
        assertEquals("Species", EntityTypeEnum.SPECIES.getLabel());

        assertNotNull(EntityTypeEnum.WORLD);
        assertEquals("World", EntityTypeEnum.WORLD.getLabel());

        assertNotNull(EntityTypeEnum.REALM);
        assertEquals("Realm", EntityTypeEnum.REALM.getLabel());

        assertNotNull(EntityTypeEnum.ELEMENT);
        assertEquals("Element", EntityTypeEnum.ELEMENT.getLabel());
    }
}
