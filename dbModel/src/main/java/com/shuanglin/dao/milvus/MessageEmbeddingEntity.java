package com.shuanglin.dao.milvus;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Message embedding entity for Milvus collection
 * Note: Collection schema is managed manually in MilvusInitializer
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageEmbeddingEntity {
	private String id;

	@SerializedName(value = "userId", alternate = {"user_id"})
	private String userId;

	@SerializedName(value = "groupId", alternate = {"group_id"})
	private String groupId;

	private String storeType;

	private String modelName;

	private float[] embeddings;

	private String storeId;
}
