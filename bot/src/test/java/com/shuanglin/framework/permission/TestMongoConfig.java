package com.shuanglin.framework.permission;

import com.mongodb.client.MongoClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 测试配置
 */
@TestConfiguration
public class TestMongoConfig {

    // 使用嵌入式 MongoDB 或测试容器时需要配置
    // 这里使用 Mock 来避免实际连接

    @Bean
    public MongoTemplate mongoTemplate(MongoClient mongoClient) {
        return new MongoTemplate(mongoClient, "botDB_test");
    }
}
