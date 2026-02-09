package com.shuanglin.framework.aop;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.shuanglin.framework.annotation.PublishBus;
import com.shuanglin.framework.bus.MessageBus;
import com.shuanglin.framework.bus.event.Event;
import com.shuanglin.framework.bus.event.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * AOP 切面，用于自动将 @PublishBus 注解的方法返回值发布到消息总线
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class PublishToBusAspect {

	private final MessageBus messageBus;

	// 简单 GSON 用于类型转换
	private final Gson gson = new Gson();

	// 定义切点，拦截所有被 @PublishBus 注解的方法
	@Pointcut("@annotation(com.shuanglin.framework.annotation.PublishBus)")
	public void publishBusPointcut() {
	}

	// 在方法执行前后执行
	@Around("publishBusPointcut()")
	public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
		log.debug("AOP: Intercepted method: {}", joinPoint.getSignature());

		Object result = joinPoint.proceed();

		// 方法返回值作为消息发布
		if (result != null) {
			try {
				String resultStr = result.toString();
				log.debug("AOP: Publishing message to bus: {}", resultStr.substring(0, Math.min(100, resultStr.length())));

				// 解析为 Event 对象并发布
				Event event = parseEvent(resultStr);
				if (event != null) {
					messageBus.publish(event);
					log.debug("AOP: Message published successfully");
				}
			} catch (Exception e) {
				log.error("AOP: Error while publishing message: {}", e.getMessage(), e);
			}
		} else {
			log.warn("AOP: Method returned null, skipping");
		}

		return result;
	}

	/**
	 * 解析 JSON 字符串为 Event 对象，并设置 rawData
	 */
	private Event parseEvent(String jsonStr) {
		try {
			JsonObject jsonObj = JsonParser.parseString(jsonStr).getAsJsonObject();

			// 提取类型信息
			String postType = jsonObj.has("post_type") ? jsonObj.get("post_type").getAsString() : "";
			String messageType = jsonObj.has("message_type") ? jsonObj.get("message_type").getAsString() : "";

			log.debug("AOP: Parsing event - post_type={}, message_type={}", postType, messageType);

			// 根据类型选择目标类
			Class<? extends Event> eventClass;
			if ("message".equals(postType) && "group".equals(messageType)) {
				eventClass = GroupMessageEvent.class;
				log.debug("AOP: Using GroupMessageEvent.class");
			} else {
				eventClass = Event.class;
				log.debug("AOP: Using Event.class");
			}

			// 反序列化为 Event 对象
			Event event = gson.fromJson(jsonStr, eventClass);
			log.debug("AOP: Event deserialized, actual class={}", event.getClass().getName());

			// 设置 rawData - 使用 GSON 直接转换为 Map，避免 JsonElement 问题
			if (event != null) {
				@SuppressWarnings("unchecked")
				Map<String, Object> rawData = gson.fromJson(jsonStr, Map.class);
				event.setRawData(rawData);
				log.debug("AOP: rawData set with {} entries, message_type={}", rawData.size(), rawData.get("message_type"));
			}

			return event;
		} catch (Exception e) {
			log.error("AOP: Failed to parse event: {}", e.getMessage(), e);
			return null;
		}
	}
}
