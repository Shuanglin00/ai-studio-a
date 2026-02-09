package com.shuanglin.framework.milky.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Milky API 配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "milky")
public class MilkyApiConfig {

    /**
     * API基础URL
     */
    private String baseUrl = "http://127.0.0.1:3000";

    /**
     * Access Token
     */
    private String accessToken;

    /**
     * 超时时间（毫秒）
     */
    private Integer timeout = 5000;

    /**
     * 连接超时（毫秒）
     */
    private Integer connectTimeout = 3000;

    /**
     * 读取超时（毫秒）
     */
    private Integer readTimeout = 5000;
}
