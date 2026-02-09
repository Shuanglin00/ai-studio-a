package com.shuanglin.dao.neo4j.novel;

import com.shuanglin.dao.neo4j.novel.enums.EntityType;
import com.shuanglin.dao.neo4j.novel.enums.ItemType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 物品节点 - Neo4j实体类
 * 对应systemPrompt-3.0.md第5.2节领域实体子类型
 * 
 * 继承自EntityNode，entityType = EntityType.ITEM
 * 推荐使用复合标签 :Entity:Item
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ItemNode extends EntityNode {
    
    /**
     * 物品类型
     */
    private ItemType itemType;
    
    public ItemNode(String uuid, String name, Integer createdAtChapter, ItemType itemType) {
        super(uuid, name, EntityType.ITEM, createdAtChapter);
        this.itemType = itemType;
    }
}
