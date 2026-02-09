package com.shuanglin.ai.langchain4j.config;

import com.shuanglin.ai.langchain4j.assistant.DecomposeAssistant;
import com.shuanglin.ai.langchain4j.assistant.GeminiAssistant;
import com.shuanglin.ai.langchain4j.assistant.MiniMaxAssistant;
import com.shuanglin.ai.langchain4j.assistant.OllamaAssistant;
import com.shuanglin.ai.langchain4j.store.FilterMemoryStore;
import com.shuanglin.ai.langchain4j.config.vo.GeminiProperties;
import com.shuanglin.ai.langchain4j.config.vo.MiniMaxProperties;
import com.shuanglin.ai.langchain4j.config.vo.OllamaProperties;
import com.shuanglin.ai.langchain4j.config.vo.QwenProperties;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiStreamingChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.Resource;
import org.springframework.core.io.ClassPathResource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;

/**
 * 模型bean管理
 *
 * @author lin
 * @date 2025/06/27
 */
@Configuration
@EnableConfigurationProperties({GeminiProperties.class, QwenProperties.class, OllamaProperties.class, MiniMaxProperties.class})
public class ApiModelsConfiguration {

	@Bean("decomposeLanguageModel")
	public OllamaChatModel decomposeLanguageModel(OllamaProperties ollamaProperties) {
		return OllamaChatModel.builder()
				.baseUrl(ollamaProperties.getUrl())
				.temperature(ollamaProperties.getTemperature()) // 模型温度，控制模型生成的随机性，0-1之间，越大越多样性
				.logRequests(true)
				.logResponses(true)
				.modelName(ollamaProperties.getModelName())
                .timeout(ollamaProperties.getTimeout())
		 		.build();
	}

	/**
	 * 加载知识图谱系统提示词
	 * @return kgKnowlage.md文件内容
	 */
	private String loadKgKnowlagePrompt() {
		try {
			// 从类路径加载资源文件
			ClassPathResource resource = new ClassPathResource("prompt/SystemPrompt2.md");
			InputStream inputStream = resource.getInputStream();
			StringBuilder content = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
			String line;
			while ((line = reader.readLine()) != null) {
				content.append(line).append("\n");
			}
			return content.toString();
		} catch (IOException e) {
			throw new RuntimeException("加载知识图谱提示词文件失败: prompt/SystemPrompt2.md", e);
		}
	}

	@Bean("chatLanguageModel")
	public OllamaChatModel chatLanguageModel(OllamaProperties ollamaProperties) {
		return OllamaChatModel.builder()
				.baseUrl(ollamaProperties.getUrl())
				.temperature(ollamaProperties.getTemperature())
				.timeout(Duration.ofMinutes(10)) // 模型温度，控制模型生成的随机性，0-1之间，越大越多样性
				.logRequests(true)
				.logResponses(true)
				.modelName(ollamaProperties.getModelName())
                .timeout(ollamaProperties.getTimeout())
				.build();
	}

	@Bean
	public OllamaStreamingChatModel chatStreamingLanguageModel(OllamaProperties ollamaProperties) {
		return OllamaStreamingChatModel.builder()
				.baseUrl(ollamaProperties.getUrl())
				.temperature(ollamaProperties.getTemperature())
				.timeout(Duration.ofMinutes(10)) // 模型温度，控制模型生成的随机性，0-1之间，越大越多样性
				.logRequests(true)
				.logResponses(true)
				.modelName(ollamaProperties.getModelName())
                .timeout(ollamaProperties.getTimeout())
				.build();
	}


	@Bean
	public OllamaAssistant ollamaAssistant(@Qualifier("chatLanguageModel") OllamaChatModel chatLanguageModel,
										   OllamaStreamingChatModel chatStreamingLanguageModel,
										   RetrievalAugmentor chatRetrievalAugmentor,
										   FilterMemoryStore filterMemoryStore) {

		return AiServices.builder(OllamaAssistant.class)
				.chatModel(chatLanguageModel)
				.streamingChatModel(chatStreamingLanguageModel)
				.chatMemoryProvider(modelId -> MessageWindowChatMemory.builder()
						.id(modelId)
						.maxMessages(10)
						.chatMemoryStore(filterMemoryStore)
						.build())
				.retrievalAugmentor(chatRetrievalAugmentor)
				.build();
	}

	@Bean
	GoogleAiGeminiChatModel googleAiGeminiChatModel(GeminiProperties geminiProperties, List<ChatModelListener> chatModelListenerList) {
		return GoogleAiGeminiChatModel.builder()
				.apiKey(geminiProperties.getApiKey())
				.modelName(geminiProperties.getModelName())
				.temperature(geminiProperties.getTemperature())
				.topP(geminiProperties.getTopP())
				.topK(geminiProperties.getTopK())
				.listeners(chatModelListenerList)
				.timeout(Duration.ofMinutes(10))
				.build();
	}

	@Bean
	GoogleAiGeminiStreamingChatModel googleAiGeminiStreamingChatModel(GeminiProperties geminiProperties, List<ChatModelListener> chatModelListenerList) {
		return GoogleAiGeminiStreamingChatModel.builder()
				.apiKey(geminiProperties.getApiKey())
				.modelName(geminiProperties.getModelName())
				.temperature(geminiProperties.getTemperature())
				.topP(geminiProperties.getTopP())
				.topK(geminiProperties.getTopK())
				.listeners(chatModelListenerList)
				.timeout(Duration.ofMinutes(10))
				.build();
	}

	@Bean(name = "miniMaxChatModel")
	public OpenAiChatModel miniMaxChatModel(MiniMaxProperties miniMaxProperties) {
		return OpenAiChatModel.builder()
				.baseUrl(miniMaxProperties.getBaseUrl())
				.apiKey(miniMaxProperties.getApiKey())
				.modelName(miniMaxProperties.getModelName())
				.temperature(miniMaxProperties.getTemperature())
				.logRequests(true)
				.logResponses(true)
				.build();
	}

	@Bean(name = "miniMaxStreamingChatModel")
	public OpenAiStreamingChatModel miniMaxStreamingChatModel(MiniMaxProperties miniMaxProperties) {
		return OpenAiStreamingChatModel.builder()
				.baseUrl(miniMaxProperties.getBaseUrl())
				.apiKey(miniMaxProperties.getApiKey())
				.modelName(miniMaxProperties.getModelName())
				.temperature(miniMaxProperties.getTemperature())
				.logRequests(true)
				.logResponses(true)
				.build();
	}

	/**
	 * 创建 DecomposeAssistant Bean，使用 kgKnowlage.md 作为 system prompt
	 * @param decomposeLanguageModel 分解任务使用的语言模型
	 * @return DecomposeAssistant 实例
	 */
	@Bean
	public DecomposeAssistant decomposeAssistant(@Qualifier("miniMaxChatModel") OpenAiChatModel decomposeLanguageModel) {
		// 加载知识图谱系统提示词
		String systemPrompt = loadKgKnowlagePrompt();

		return AiServices.builder(DecomposeAssistant.class)
				.chatModel(decomposeLanguageModel)
				.systemMessageProvider(chatMemoryId -> systemPrompt)
				.build();
	}

	@Bean
	public GeminiAssistant geminiAssistant(GoogleAiGeminiChatModel chatModel,
										   GoogleAiGeminiStreamingChatModel googleAiGeminiStreamingChatModel,
										   FilterMemoryStore filterMemoryStore,
										   RetrievalAugmentor chatRetrievalAugmentor

	) {

		return AiServices.builder(GeminiAssistant.class)
				.chatModel(chatModel)
				.streamingChatModel(googleAiGeminiStreamingChatModel)
				.chatMemoryProvider(memoryId -> MessageWindowChatMemory.builder()
						.id(memoryId)
						.maxMessages(1) //自行存储message
						.chatMemoryStore(filterMemoryStore)
						.build())
				.retrievalAugmentor(chatRetrievalAugmentor)
				.build();
	}

	@Bean
	public MiniMaxAssistant miniMaxAssistant(OpenAiChatModel miniMaxChatModel,
										   OpenAiStreamingChatModel miniMaxStreamingChatModel,
										   FilterMemoryStore filterMemoryStore,
										   RetrievalAugmentor chatRetrievalAugmentor) {

		return AiServices.builder(MiniMaxAssistant.class)
				.chatModel(miniMaxChatModel)
				.streamingChatModel(miniMaxStreamingChatModel)
				.chatMemoryProvider(memoryId -> MessageWindowChatMemory.builder()
						.id(memoryId)
						.maxMessages(1) //自行存储message
						.chatMemoryStore(filterMemoryStore)
						.build())
				.retrievalAugmentor(chatRetrievalAugmentor)
				.build();
	}


}