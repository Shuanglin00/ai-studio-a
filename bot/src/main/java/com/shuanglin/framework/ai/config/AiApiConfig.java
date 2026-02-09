package com.shuanglin.framework.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI 服务配置类
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai")
public class AiApiConfig {

    /**
     * AI 服务基础 URL
     */
    private String baseUrl = "http://localhost:8081";

    /**
     * 请求超时时间（毫秒）
     */
    private Integer timeout = 60000;

    /**
     * 连接超时时间（毫秒）
     */
    private Integer connectTimeout = 5000;

    /**
     * 读取超时时间（毫秒）
     */
    private Integer readTimeout = 60000;
}
