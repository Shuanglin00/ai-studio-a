package com.shuanglin.framework.onebot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * OneBot API配置属性
 */
@Data
@ConfigurationProperties(prefix = "onebot.api")
public class OneBotApiProperties {

    /**
     * OneBot HTTP API基础URL
     */
    private String baseUrl = "http://127.0.0.1:3000";

    /**
     * 请求超时时间（毫秒）
     */
    private Integer timeout = 5000;
}
