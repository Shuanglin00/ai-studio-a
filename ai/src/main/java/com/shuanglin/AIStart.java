package com.shuanglin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication
@ComponentScan(
    basePackages = {"com.shuanglin.ai", "com.shuanglin.common", "com.shuanglin.dao"},
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = "com.shuanglin.train.*"
    )
)
public class AIStart {
	public static void main(String[] args) {
//		// 1. 【核心步骤】在程序早期设置代理的系统属性
//		// 这会影响整个 JVM 的网络连接
//		String proxyHost = "127.0.0.1";
//		String proxyPort = "7897"; // 代理端口通常是数字，所以用字符串
//
//		System.setProperty("http.proxyHost", proxyHost);
//		System.setProperty("http.proxyPort", proxyPort);
//
//		// 如果您的代理也需要为 HTTPS 流量服务（Google API 使用 HTTPS），也设置这些属性
//		System.setProperty("https.proxyHost", proxyHost);
//		System.setProperty("https.proxyPort", proxyPort);
		SpringApplication.run(AIStart.class, args);
	}
}
