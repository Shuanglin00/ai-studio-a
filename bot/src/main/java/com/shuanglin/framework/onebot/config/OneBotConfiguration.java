package com.shuanglin.framework.onebot.config;

import com.shuanglin.framework.onebot.builder.GroupMessageBuilder;
import com.shuanglin.framework.onebot.sender.MessageSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * OneBot配置类
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(OneBotApiProperties.class)
@RequiredArgsConstructor
public class OneBotConfiguration {

    private final MessageSender messageSender;

    /**
     * 初始化MessageBuilder
     */
    @PostConstruct
    public void init() {
        GroupMessageBuilder.setMessageSender(messageSender);
        log.info("OneBot Configuration initialized with API: {}", 
                messageSender.getClass().getSimpleName());
    }
}
