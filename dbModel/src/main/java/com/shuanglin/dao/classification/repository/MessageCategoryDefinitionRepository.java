package com.shuanglin.dao.classification.repository;

import com.shuanglin.dao.classification.MessageCategoryDefinition;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 分类标签定义Repository
 */
@Repository
public interface MessageCategoryDefinitionRepository extends MongoRepository<MessageCategoryDefinition, String> {

    /**
     * 根据编码查询
     */
    Optional<MessageCategoryDefinition> findByCode(String code);

    /**
     * 查询所有启用的标签
     */
    List<MessageCategoryDefinition> findByEnabledTrue();

    /**
     * 根据优先级查询
     */
    List<MessageCategoryDefinition> findByEnabledTrueOrderByPriorityAsc();

    /**
     * 检查编码是否存在
     */
    boolean existsByCode(String code);

    /**
     * 根据编码删除
     */
    void deleteByCode(String code);
}
