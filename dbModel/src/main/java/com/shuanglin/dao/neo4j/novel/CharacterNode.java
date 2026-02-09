package com.shuanglin.dao.neo4j.novel;

import com.shuanglin.dao.neo4j.novel.enums.EntityType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 角色节点 - Neo4j实体类
 * 对应systemPrompt-3.0.md第5.2节领域实体子类型
 * 
 * 继承自EntityNode，entityType = EntityType.CHARACTER
 * 推荐使用复合标签 :Entity:Character
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CharacterNode extends EntityNode {
    
    /**
     * 别名、称号列表
     */
    private List<String> alias;
    
    public CharacterNode(String uuid, String name, Integer createdAtChapter, List<String> alias) {
        super(uuid, name, EntityType.CHARACTER, createdAtChapter);
        this.alias = alias;
    }
}
