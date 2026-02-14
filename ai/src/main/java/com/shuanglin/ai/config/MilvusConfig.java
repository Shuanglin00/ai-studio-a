package com.shuanglin.ai.config;

import com.shuanglin.ai.langchain4j.config.vo.MilvusProperties;
import io.milvus.v2.client.MilvusClientV2;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Milvus client configuration
 * Replaces the auto-configuration that was previously provided by milvus-plus
 */
@Configuration
@EnableConfigurationProperties({MilvusProperties.class})
public class MilvusConfig {

    @Bean
    public MilvusClientV2 milvusClientV2(MilvusProperties milvusProperties) throws Exception {
        // Use reflection to get the correct ConnectConfig builder
        Class<?> connectConfigClass = Class.forName("io.milvus.v2.client.ConnectConfig");
        Object builder = connectConfigClass.getMethod("builder").invoke(null);
        builder.getClass().getMethod("uri", String.class).invoke(builder, milvusProperties.getUri());

        if (milvusProperties.getUsername() != null && !milvusProperties.getUsername().isEmpty()) {
            builder.getClass().getMethod("token", String.class).invoke(builder,
                    milvusProperties.getUsername() + ":" + milvusProperties.getPassword());
        }

        Object config = builder.getClass().getMethod("build").invoke(builder);
        return MilvusClientV2.class.getConstructor(connectConfigClass).newInstance(config);
    }
}
