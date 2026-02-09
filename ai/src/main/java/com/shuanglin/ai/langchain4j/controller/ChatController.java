package com.shuanglin.ai.langchain4j.controller;

import cn.hutool.core.util.IdUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.shuanglin.ai.langchain4j.assistant.GeminiAssistant;
import com.shuanglin.ai.langchain4j.assistant.MiniMaxAssistant;
import com.shuanglin.ai.langchain4j.assistant.OllamaAssistant;
import com.shuanglin.ai.langchain4j.config.DocumentInitializer;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * AI 聊天控制器
 * 提供 AI 对话服务的 REST API 接口
 */
@Slf4j
@RestController
@RequestMapping("/api/ai")
public class ChatController {

	@Autowired
	private GeminiAssistant geminiAssistant;

	@Autowired
	private OllamaAssistant ollamaAssistant;

	@Autowired
	private MiniMaxAssistant miniMaxAssistant;

	@Resource
	private DocumentInitializer documentInitializer;

	@Resource
	Gson gson;

	@PostMapping("/ask")
	public String ask(@RequestBody String message) {
		JsonObject params = gson.fromJson(message,JsonObject.class).getAsJsonObject();
		// 日志入口
		params.addProperty("messageId",IdUtil.getSnowflakeNextIdStr());
		log.info("Received chat request with params: {}", params);
		String answer= ollamaAssistant.chat(params,params.get("message").getAsString());
		log.info("Chat response generated successfully");
		return answer;
	}

	@PostMapping("/ask/minimax")
	public String askMiniMax(@RequestBody String message) {
		JsonObject params = gson.fromJson(message,JsonObject.class).getAsJsonObject();
		// 日志入口
		params.addProperty("messageId",IdUtil.getSnowflakeNextIdStr());
		String answer= miniMaxAssistant.chat(params,params.get("message").getAsString());
		return answer;
	}

	/**
	 * 群聊对话接口
	 * @param request 请求参数，包含 memoryId 和 question
	 * @return AI 回答
	 */
	@PostMapping("/group-chat")
	public String groupChat(@RequestBody String request) {
		try {
			JsonObject params = gson.fromJson(request, JsonObject.class);
			JsonObject memoryId = params.getAsJsonObject("memoryId");
			String question = params.get("question").getAsString();

			log.info("Received group chat request, memoryId: {}, question: {}", memoryId, question);

			// 使用 GeminiAssistant 进行群聊
			String answer = geminiAssistant.groupChat(memoryId, question);

			log.info("Group chat response generated successfully");
			return answer;
		} catch (Exception e) {
			log.error("Error processing group chat request: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to process group chat request", e);
		}
	}

	@PostMapping("/readFile")
	public void readDocumentFromStream(@RequestParam("file") MultipartFile multiFile) {
		try {

			// 获取文件名
			String fileName = multiFile.getOriginalFilename();
			// 获取文件后缀
			assert fileName != null;
			String prefix = fileName.substring(fileName.lastIndexOf("."));
			// 若需要防止生成的临时文件重复,可以在文件名后添加随机码
			File file = File.createTempFile(fileName, prefix);
			multiFile.transferTo(file);

			String s = documentInitializer.readFile(new JsonObject(),file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@PostMapping("/read")
	public String read(@RequestBody String body) {
		JsonObject params = new JsonObject();
		params.addProperty("userId", "1751649231");
		params.addProperty("modelName", "123");
		documentInitializer.read(params, body);
		return "OK";
	}
}