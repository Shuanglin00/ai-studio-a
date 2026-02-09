package com.shuanglin.framework.controller;

import com.shuanglin.framework.annotation.PublishBus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

/**
 * OneBot Webhook 接收控制器
 *
 * 接收 OneBot 推送的各类事件消息，使用 @PublishBus 注解通过 AOP 发布到消息总线。
 */
@Slf4j
@RestController
public class DemoController {

	/**
	 * 接收 OneBot Webhook 消息
	 *
	 * 使用 byte[] 接收原始请求体，通过 @PublishBus 注解由 AOP 发布到消息总线
	 *
	 * @param body 原始请求体字节数组
	 * @return 响应数据
	 */
	@PostMapping(value = "/bot")
	@PublishBus
	public String handleWebhook(@RequestBody byte[] body) {
		log.info("Received webhook payload");
		return new String(body, StandardCharsets.UTF_8);
	}
}
