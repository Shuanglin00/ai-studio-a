package com.shuanglin.framework.onebot11.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * OneBot 11 配置属性
 * 用于配置 OneBot 11 客户端的连接参数
 *
 * @author Shuanglin
 * @since 1.0
 */
@Data
@ConfigurationProperties(prefix = "onebot11")
public class OneBot11Properties {

    /**
     * API 基础 URL
     * 默认: http://127.0.0.1:3000
     */
    private String baseUrl = "http://127.0.0.1:3000";

    /**
     * 访问令牌（可选）
     * 用于 API 认证
     */
    private String accessToken;

    /**
     * 连接超时（毫秒）
     * 默认: 5000ms
     */
    private Long connectTimeout = 5000L;

    /**
     * 读取超时（毫秒）
     * 默认: 10000ms
     */
    private Long readTimeout = 10000L;

    /**
     * 写入超时（毫秒）
     * 默认: 10000ms
     */
    private Long writeTimeout = 10000L;

    /**
     * 是否启用自动重试
     * 默认: true
     */
    private Boolean retryEnabled = true;

    /**
     * 最大重试次数
     * 默认: 3
     */
    private Integer maxRetries = 3;

    /**
     * 重试延迟（毫秒）
     * 默认: 1000ms
     */
    private Long retryDelay = 1000L;

    /**
     * 重试延迟倍数（指数退避）
     * 默认: 2.0
     */
    private Double retryMultiplier = 2.0;
}
