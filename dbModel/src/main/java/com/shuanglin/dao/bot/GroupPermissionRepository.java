package com.shuanglin.dao.bot;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 群聊权限配置 Repository
 */
@Repository
public interface GroupPermissionRepository extends MongoRepository<GroupPermission, String> {

    List<GroupPermission> findByGroupId(String groupId);

    Optional<GroupPermission> findByGroupIdAndCommandName(String groupId, String commandName);

    List<GroupPermission> findByGroupIdAndEnabled(String groupId, boolean enabled);

    void deleteByGroupIdAndCommandName(String groupId, String commandName);

    void deleteByGroupId(String groupId);

    boolean existsByGroupIdAndCommandName(String groupId, String commandName);
}
