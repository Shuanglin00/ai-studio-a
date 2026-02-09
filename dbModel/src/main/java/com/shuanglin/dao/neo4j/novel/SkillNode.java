package com.shuanglin.dao.neo4j.novel;

import com.shuanglin.dao.neo4j.novel.enums.EntityType;
import com.shuanglin.dao.neo4j.novel.enums.SkillType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 技能节点 - Neo4j实体类
 * 对应systemPrompt-3.0.md第5.2节领域实体子类型
 * 
 * 继承自EntityNode，entityType = EntityType.SKILL
 * 推荐使用复合标签 :Entity:Skill
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SkillNode extends EntityNode {
    
    /**
     * 技能类型
     */
    private SkillType skillType;
    
    public SkillNode(String uuid, String name, Integer createdAtChapter, SkillType skillType) {
        super(uuid, name, EntityType.SKILL, createdAtChapter);
        this.skillType = skillType;
    }
}
