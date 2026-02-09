package com.shuanglin.dao.bot;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 群聊配置Repository
 */
@Repository
public interface GroupConfigurationRepository extends MongoRepository<GroupConfiguration, String> {

    /**
     * 根据群号查询配置
     *
     * @param groupId 群号
     * @return 群聊配置
     */
    Optional<GroupConfiguration> findByGroupId(String groupId);

    /**
     * 查询启用了指定角色的群聊
     *
     * @param roleName 角色名称
     * @return 群聊配置列表
     */
    List<GroupConfiguration> findByEnabledRolesContaining(String roleName);

    /**
     * 检查群号是否存在配置
     *
     * @param groupId 群号
     * @return 是否存在
     */
    boolean existsByGroupId(String groupId);
}
