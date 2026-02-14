package com.shuanglin.ai.langchain4j.rag.embedding.model;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class EmbeddingConfig {

	@Value("${langchain4j.models.ollama.url:http://localhost:11434}")
	private String ollamaUrl;

	@Bean("langchain4jEmbeddingModel")
	@Primary
	public EmbeddingModel embeddingModel() {
		// 使用 Ollama 本地 embedding 模型
		return OllamaEmbeddingModel.builder()
				.baseUrl(ollamaUrl)
				.modelName("bge-m3")
				.timeout(java.time.Duration.ofMinutes(5))
				.build();
	}
}
