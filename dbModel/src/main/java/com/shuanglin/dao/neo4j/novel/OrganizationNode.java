package com.shuanglin.dao.neo4j.novel;

import com.shuanglin.dao.neo4j.novel.enums.EntityType;
import com.shuanglin.dao.neo4j.novel.enums.OrganizationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 组织节点 - Neo4j实体类
 * 对应systemPrompt-3.0.md第5.2节领域实体子类型
 * 
 * 继承自EntityNode，entityType = EntityType.ORGANIZATION
 * 推荐使用复合标签 :Entity:Organization
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrganizationNode extends EntityNode {
    
    /**
     * 组织类型
     */
    private OrganizationType orgType;
    
    public OrganizationNode(String uuid, String name, Integer createdAtChapter, OrganizationType orgType) {
        super(uuid, name, EntityType.ORGANIZATION, createdAtChapter);
        this.orgType = orgType;
    }
}
