package com.shuanglin.ai.langchain4j.repository;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.shuanglin.ai.langchain4j.config.vo.MilvusProperties;
import com.shuanglin.dao.milvus.MessageEmbeddingEntity;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.vector.request.InsertReq;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.request.data.BaseVector;
import io.milvus.v2.service.vector.request.data.FloatVec;
import io.milvus.v2.service.vector.response.SearchResp;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Message embedding repository using milvus-sdk directly
 */
@Slf4j
@Repository
public class MessageEmbeddingRepository {

    @Resource
    private MilvusClientV2 milvusClientV2;

    @Resource
    private MilvusProperties milvusProperties;

    @Resource
    private Gson gson;

    /**
     * Search embeddings by vector
     *
     * @param floats    the query vector
     * @param topK      number of results to return
     * @param filterMap optional filter conditions
     * @return list of search results (outer list = query vectors, inner list = results per query)
     */
    public List<List<SearchResp.SearchResult>> search(List<Float> floats, int topK, Map<String, Object> filterMap) {
        SearchReq.SearchReqBuilder builder = SearchReq.builder()
                .collectionName(milvusProperties.getMessageCollectionName())
                .data(Collections.singletonList(new FloatVec(floats)))
                .topK(topK);

        if (filterMap != null && !filterMap.isEmpty()) {
            builder.filterTemplateValues(filterMap);
        }

        SearchReq searchRequest = builder.build();
        SearchResp searchResp = milvusClientV2.search(searchRequest);
        return searchResp.getSearchResults();
    }

    /**
     * Search embeddings by vector (simple version)
     *
     * @param floats the query vector
     * @param topK   number of results to return
     * @return list of search results
     */
    public List<List<SearchResp.SearchResult>> search(List<Float> floats, int topK) {
        return search(floats, topK, null);
    }

    /**
     * Search embeddings by multiple vectors
     *
     * @param vectors   list of query vectors
     * @param topK     number of results to return
     * @param filterMap optional filter conditions
     * @return list of search results
     */
    public List<List<SearchResp.SearchResult>> searchBatch(List<BaseVector> vectors, int topK, Map<String, Object> filterMap) {
        SearchReq.SearchReqBuilder builder = SearchReq.builder()
                .collectionName(milvusProperties.getMessageCollectionName())
                .data(vectors)
                .topK(topK);

        if (filterMap != null && !filterMap.isEmpty()) {
            builder.filterTemplateValues(filterMap);
        }

        SearchReq searchRequest = builder.build();
        SearchResp searchResp = milvusClientV2.search(searchRequest);
        return searchResp.getSearchResults();
    }

    /**
     * Insert embedding data into collection
     *
     * @param entity the embedding entity to insert
     */
    public void insert(MessageEmbeddingEntity entity) {
        JsonObject jsonObject = gson.toJsonTree(entity).getAsJsonObject();
        InsertReq insertRequest = InsertReq.builder()
                .data(Collections.singletonList(jsonObject))
                .collectionName("chatEmbeddingCollection")
                .build();
        milvusClientV2.insert(insertRequest);
    }

    /**
     * Get storeIds from search results
     *
     * @param searchResults search results from milvus
     * @return list of storeIds
     */
    public List<String> extractStoreIds(List<List<SearchResp.SearchResult>> searchResults) {
        return searchResults.stream()
                .flatMap(List::stream)
                .map(result -> result.getEntity().get("storeId").toString())
                .collect(Collectors.toList());
    }
}
