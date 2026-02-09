package com.shuanglin.dao.neo4j.novel;

import com.shuanglin.dao.neo4j.novel.enums.EntityType;
import com.shuanglin.dao.neo4j.novel.enums.LocationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 地点节点 - Neo4j实体类
 * 对应systemPrompt-3.0.md第5.2节领域实体子类型
 * 
 * 继承自EntityNode，entityType = EntityType.LOCATION
 * 推荐使用复合标签 :Entity:Location
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LocationNode extends EntityNode {
    
    /**
     * 地点类型
     */
    private LocationType locationType;
    
    public LocationNode(String uuid, String name, Integer createdAtChapter, LocationType locationType) {
        super(uuid, name, EntityType.LOCATION, createdAtChapter);
        this.locationType = locationType;
    }
}
