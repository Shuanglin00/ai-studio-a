package com.shuanglin.dao.bot;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 机器人角色Repository
 */
@Repository
public interface BotRoleRepository extends MongoRepository<BotRole, String> {

    /**
     * 根据角色名称查询
     *
     * @param roleName 角色名称
     * @return 角色对象
     */
    Optional<BotRole> findByRoleName(String roleName);

    /**
     * 查询所有启用的角色
     *
     * @param isActive 启用状态
     * @return 角色列表
     */
    List<BotRole> findByIsActive(Boolean isActive);

    /**
     * 检查角色名称是否存在
     *
     * @param roleName 角色名称
     * @return 是否存在
     */
    boolean existsByRoleName(String roleName);
}
