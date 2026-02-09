package com.shuanglin.framework.config;

import com.shuanglin.framework.onebot.config.OneBotApiProperties;
import com.shuanglin.framework.onebot.config.RetryProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

@Configuration
@EnableConfigurationProperties({OneBotApiProperties.class, RetryProperties.class, BotProperties.class})
public class FrameworkConfig {

	@Bean
	public ExpressionParser expressionParser() {
		return new SpelExpressionParser();
	}
}