package com.shuanglin.dao.bot;

import com.shuanglin.common.enums.CommandType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 全局权限配置 Repository
 */
@Repository
public interface GlobalPermissionRepository extends MongoRepository<GlobalPermission, String> {

    Optional<GlobalPermission> findByCommandName(String commandName);

    List<GlobalPermission> findByEnabled(boolean enabled);

    List<GlobalPermission> findByCommandTypeAndEnabled(CommandType commandType, boolean enabled);

    boolean existsByCommandName(String commandName);
}
