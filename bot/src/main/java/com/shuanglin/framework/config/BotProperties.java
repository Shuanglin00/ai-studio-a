package com.shuanglin.framework.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Bot 框架配置属性
 */
@Data
@ConfigurationProperties(prefix = "bot")
public class BotProperties {

    private FrameworkConfig framework = new FrameworkConfig();
    private PigConfig pig = new PigConfig();
    private LogConfig log = new LogConfig();

    @Data
    public static class FrameworkConfig {
        private PermissionConfig permission = new PermissionConfig();
        private CompatibilityConfig compatibility = new CompatibilityConfig();
    }

    @Data
    public static class PermissionConfig {
        private boolean enabled = true;
    }

    @Data
    public static class CompatibilityConfig {
        private boolean legacyMode = false;
    }

    @Data
    public static class PigConfig {
        /**
         * 猪图片目录路径
         */
        private String imagePath = "${user.dir}/resources/pigs";
    }

    @Data
    public static class LogConfig {
        /**
         * 日志输出目录
         */
        private String path = "${user.dir}/../APPS/log";
        /**
         * 日志级别
         */
        private String level = "INFO";
    }
}
