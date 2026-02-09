package com.shuanglin.framework.onebot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * OneBot API 重试配置
 */
@Data
@ConfigurationProperties(prefix = "onebot.api.retry")
public class RetryProperties {

    /**
     * 最大重试次数
     */
    private int maxAttempts = 3;

    /**
     * 初始重试延迟（毫秒）
     */
    private long delayMs = 1000;

    /**
     * 延迟倍数（指数退避）
     */
    private double multiplier = 2.0;
}
