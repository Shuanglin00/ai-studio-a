package com.shuanglin.ai.langchain4j.config.vo;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = MilvusProperties.PREFIX)
public class MilvusProperties {
	public static final String PREFIX = "milvus";

	private String uri;

	private String host;

	private Integer port;

	private String username;

	private String password;

	private String dbName;

	private Integer topK;

	/**
	 * 向量集合名（RAG向量表名）
	 */
	private String messageCollectionName;

}
