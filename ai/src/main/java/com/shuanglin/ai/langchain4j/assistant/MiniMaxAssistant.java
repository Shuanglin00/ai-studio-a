package com.shuanglin.ai.langchain4j.assistant;

import com.google.gson.JsonObject;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface MiniMaxAssistant {

	/**
	 * 聊天
	 * @param memoryId 设定角色
	 * @param question 原始问题
	 * @return
	 */
	@UserMessage(value = "{{question}}")
	String chat(@MemoryId JsonObject memoryId,
			@V("question") String question);


	@UserMessage(value = """
			{{question}}
			""")
	String groupChat(
			@MemoryId JsonObject memoryId,
			@V("question") String question);

}