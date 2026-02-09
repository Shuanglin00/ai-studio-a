package com.shuanglin.framework.session;

import com.shuanglin.dao.bot.UserSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 会话管理器
 * 管理用户级别的会话状态
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SessionManager {

    @Qualifier("userSessionRedisTemplate")
    private final RedisTemplate<String, UserSession> userSessionRedisTemplate;

    private static final String SESSION_PREFIX = "user:session:";
    private static final long SESSION_TTL = 2; // 2小时

    /**
     * 获取用户会话
     *
     * @param groupId 群号
     * @param userId  用户ID
     * @return 用户会话，不存在则创建
     */
    public UserSession getUserSession(String groupId, String userId) {
        String key = buildSessionKey(groupId, userId);
        
        UserSession session = userSessionRedisTemplate.opsForValue().get(key);
        if (session == null) {
            // 创建新会话
            session = UserSession.builder()
                    .groupId(groupId)
                    .userId(userId)
                    .build();
            userSessionRedisTemplate.opsForValue().set(key, session, SESSION_TTL, TimeUnit.HOURS);
        }
        
        return session;
    }

    /**
     * 更新用户选择的模型
     *
     * @param groupId    群号
     * @param userId     用户ID
     * @param modelName  模型名称
     */
    public void updateUserModel(String groupId, String userId, String modelName) {
        UserSession session = getUserSession(groupId, userId);
        session.setCurrentModel(modelName);
        
        String key = buildSessionKey(groupId, userId);
        userSessionRedisTemplate.opsForValue().set(key, session, SESSION_TTL, TimeUnit.HOURS);
        
        log.info("Updated user model: groupId={}, userId={}, model={}", groupId, userId, modelName);
    }

    /**
     * 清除会话
     *
     * @param groupId 群号
     * @param userId  用户ID
     */
    public void clearSession(String groupId, String userId) {
        String key = buildSessionKey(groupId, userId);
        userSessionRedisTemplate.delete(key);
        log.info("Cleared session: groupId={}, userId={}", groupId, userId);
    }

    /**
     * 构建会话Key
     */
    private String buildSessionKey(String groupId, String userId) {
        return SESSION_PREFIX + groupId + ":" + userId;
    }
}
