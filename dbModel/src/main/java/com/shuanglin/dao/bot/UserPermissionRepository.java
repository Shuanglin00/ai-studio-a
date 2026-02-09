package com.shuanglin.dao.bot;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户权限配置 Repository
 */
@Repository
public interface UserPermissionRepository extends MongoRepository<UserPermission, String> {

    Optional<UserPermission> findByUserId(String userId);

    List<UserPermission> findByBotAdminTrue();

    boolean existsByUserId(String userId);
}
