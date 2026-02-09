package com.shuanglin.dao.bot;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 指令Repository
 */
@Repository
public interface CommandRepository extends MongoRepository<Command, String> {

    /**
     * 根据指令名称查询
     *
     * @param commandName 指令名称
     * @return 指令对象
     */
    Optional<Command> findByCommandName(String commandName);

    /**
     * 根据触发前缀查询
     *
     * @param triggerPrefix 触发前缀
     * @return 指令对象
     */
    Optional<Command> findByTriggerPrefix(String triggerPrefix);

    /**
     * 根据角色查询指令列表
     *
     * @param role 角色名称
     * @return 指令列表
     */
    List<Command> findByRole(String role);

    /**
     * 检查指令名称是否存在
     *
     * @param commandName 指令名称
     * @return 是否存在
     */
    boolean existsByCommandName(String commandName);

    /**
     * 根据指令名称删除所有匹配文档（用于清理重复数据）
     *
     * @param commandName 指令名称
     */
    void deleteByCommandName(String commandName);
}