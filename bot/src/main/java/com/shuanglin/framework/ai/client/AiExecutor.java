package com.shuanglin.framework.ai.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.shuanglin.framework.ai.config.AiApiConfig;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * AI 服务客户端
 * 通过 HTTP 调用 AI 模块提供的 REST API
 */
@Slf4j
@Component
public class AiExecutor {

    private final AiApiConfig config;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Gson gson;

    public AiExecutor(AiApiConfig config, ObjectMapper objectMapper, Gson gson) {
        this.config = config;
        this.objectMapper = objectMapper;
        this.gson = gson;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(config.getConnectTimeout(), TimeUnit.MILLISECONDS)
                .readTimeout(config.getReadTimeout(), TimeUnit.MILLISECONDS)
                .writeTimeout(config.getTimeout(), TimeUnit.MILLISECONDS)
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request.Builder requestBuilder = original.newBuilder()
                            .header("Content-Type", "application/json")
                            .header("Accept", "application/json");
                    requestBuilder.method(original.method(), original.body());
                    return chain.proceed(requestBuilder.build());
                })
                .build();
    }

    /**
     * 发送 POST 请求并获取响应
     */
    private <T> T post(String apiPath, Object requestBody, Class<T> responseClass) throws IOException {
        String url = config.getBaseUrl() + apiPath;
        RequestBody body = RequestBody.create(
                objectMapper.writeValueAsString(requestBody),
                MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("AI API 返回异常: " + response.code() + ", " + response.message());
            }

            String responseBody = response.body() != null ? response.body().string() : null;
            if (responseBody == null || responseBody.isEmpty()) {
                return null;
            }

            return objectMapper.readValue(responseBody, responseClass);
        }
    }

    /**
     * 发送 POST 请求，直接返回字符串响应
     */
    private String postString(String apiPath, Object requestBody) throws IOException {
        String url = config.getBaseUrl() + apiPath;
        RequestBody body = RequestBody.create(
                objectMapper.writeValueAsString(requestBody),
                MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("AI API 返回异常: " + response.code() + ", " + response.message());
            }

            return response.body() != null ? response.body().string() : null;
        }
    }

    // ==================== 对话 API ====================

    /**
     * 群聊对话（RAG）
     * 使用 Gemini 模型进行群聊对话，支持上下文记忆
     *
     * @param memoryId 记忆 ID，包含 userId、groupId、messageId 等信息
     * @param question 用户问题
     * @return AI 回答
     */
    public String groupChat(JsonObject memoryId, String question) throws IOException {
        JsonObject requestBody = new JsonObject();
        requestBody.add("memoryId", memoryId);
        requestBody.addProperty("question", question);
        log.info("Calling AI group-chat API, memoryId: {}, question: {}", memoryId, question);
        return postString("/api/ai/group-chat", requestBody);
    }

    /**
     * Ollama 对话
     * 使用本地 Ollama 模型进行对话
     *
     * @param message 用户消息
     * @return AI 回答
     */
    public String ask(String message) throws IOException {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("message", message);
        log.info("Calling AI ask API (Ollama), message: {}", message);
        return postString("/api/ai/ask", requestBody);
    }

    /**
     * MiniMax 对话
     * 使用 MiniMax-M2 模型进行对话
     *
     * @param message 用户消息
     * @return AI 回答
     */
    public String askMiniMax(String message) throws IOException {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("message", message);
        log.info("Calling AI ask-minimax API, message: {}", message);
        return postString("/api/ai/ask/minimax", requestBody);
    }

    // ==================== 文档处理 API ====================

    /**
     * 读取文档内容
     *
     * @param content 文档内容
     * @return 处理结果
     */
    public String read(String content) throws IOException {
        log.info("Calling AI read API");
        return postString("/api/ai/read", content);
    }

    // ==================== 小说知识图谱 API ====================

    /**
     * 构建小说知识图谱（GraphService 增量扫描方式）
     *
     * @param filePath    EPUB 文件路径
     * @param bookName    书籍名称
     * @param dataSource  数据源标识
     * @param chapterLimit 章节数量限制（0表示不限制）
     * @return 处理结果 JSON
     */
    public String buildNovelGraph(String filePath, String bookName, String dataSource, Integer chapterLimit) throws IOException {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("filePath", filePath);
        requestBody.addProperty("bookName", bookName);
        requestBody.addProperty("dataSource", dataSource);
        requestBody.addProperty("chapterLimit", chapterLimit);
        log.info("Calling AI novel graph build API, filePath: {}, bookName: {}", filePath, bookName);
        return postString("/api/ai/novel/graph/build", requestBody);
    }

    /**
     * 重放指定章节的 Cypher
     *
     * @param bookUuid     书籍 UUID
     * @param chapterIndex 章节索引
     * @return 处理结果 JSON
     */
    public String replayCypher(String bookUuid, Integer chapterIndex) throws IOException {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("bookUuid", bookUuid);
        requestBody.addProperty("chapterIndex", chapterIndex);
        log.info("Calling AI novel cypher replay API, bookUuid: {}, chapterIndex: {}", bookUuid, chapterIndex);
        return postString("/api/ai/novel/graph/replay", requestBody);
    }

    /**
     * 清理测试数据
     *
     * @param dataSource 数据源标识
     * @return 清理报告
     */
    public String cleanupNovelData(String dataSource) throws IOException {
        String url = config.getBaseUrl() + "/api/ai/novel/graph/cleanup?dataSource=" + dataSource;
        Request request = new Request.Builder()
                .url(url)
                .delete()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("AI API 返回异常: " + response.code() + ", " + response.message());
            }
            return response.body() != null ? response.body().string() : null;
        }
    }

    /**
     * 查询数据统计
     *
     * @param dataSource 数据源标识
     * @return 统计信息
     */
    public String getNovelStats(String dataSource) throws IOException {
        String url = config.getBaseUrl() + "/api/ai/novel/graph/stats?dataSource=" + dataSource;
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("AI API 返回异常: " + response.code() + ", " + response.message());
            }
            return response.body() != null ? response.body().string() : null;
        }
    }

    /**
     * 扫描并构建实体注册表（EntityStandardizer 方式）
     *
     * @param filePath EPUB 文件路径
     * @return 处理结果 JSON
     */
    public String scanEntities(String filePath) throws IOException {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("filePath", filePath);
        log.info("Calling AI entity scan API, filePath: {}", filePath);
        return postString("/api/ai/novel/entity/scan", requestBody);
    }

    /**
     * 解析别名获取实体 ID
     *
     * @param alias    别名
     * @param bookUuid 书籍 UUID
     * @return 实体 ID
     */
    public String resolveEntityAlias(String alias, String bookUuid) throws IOException {
        String url = config.getBaseUrl() + "/api/ai/novel/entity/resolve?alias=" + alias + "&bookUuid=" + bookUuid;
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("AI API 返回异常: " + response.code() + ", " + response.message());
            }
            return response.body() != null ? response.body().string() : null;
        }
    }

    /**
     * 根据实体 ID 查询实体信息
     *
     * @param entityId 实体 ID
     * @return 实体信息 JSON
     */
    public String getEntity(String entityId) throws IOException {
        String url = config.getBaseUrl() + "/api/ai/novel/entity/" + entityId;
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("AI API 返回异常: " + response.code() + ", " + response.message());
            }
            return response.body() != null ? response.body().string() : null;
        }
    }
}
