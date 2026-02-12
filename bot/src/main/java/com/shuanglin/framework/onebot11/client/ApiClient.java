package com.shuanglin.framework.onebot11.client;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.shuanglin.framework.onebot11.config.OneBot11Properties;
import com.shuanglin.framework.onebot11.exception.ApiException;
import com.shuanglin.framework.onebot11.model.response.OneBotResponse;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * HTTP API 底层客户端
 * 处理所有 HTTP 请求和响应解析
 *
 * @author Shuanglin
 * @since 1.0
 */
@Slf4j
public class ApiClient {

    private final OkHttpClient httpClient;
    private final OneBot11Properties properties;
    private final Gson gson;

    /**
     * 创建 API 客户端
     *
     * @param httpClient HTTP 客户端
     * @param properties 配置属性
     */
    public ApiClient(OkHttpClient httpClient, OneBot11Properties properties) {
        this.httpClient = httpClient;
        this.properties = properties;
        this.gson = new Gson();
    }

    /**
     * 创建带重试的 HTTP 客户端
     *
     * @param properties 配置属性
     * @return HTTP 客户端
     */
    public static OkHttpClient createHttpClient(OneBot11Properties properties) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(properties.getConnectTimeout(), TimeUnit.MILLISECONDS)
                .readTimeout(properties.getReadTimeout(), TimeUnit.MILLISECONDS)
                .writeTimeout(properties.getWriteTimeout(), TimeUnit.MILLISECONDS);

        return builder.build();
    }

    /**
     * 同步 POST 请求
     *
     * @param endpoint      API 端点
     * @param requestBody   请求体
     * @param responseClass 响应类型
     * @param <T>           响应数据类型
     * @return 响应数据
     * @throws ApiException 当 API 调用失败时
     */
    public <T> T post(String endpoint, Object requestBody, Class<T> responseClass) {
        String url = buildUrl(endpoint);
        String jsonBody = requestBody != null ? gson.toJson(requestBody) : "{}";

        log.debug("API Request: POST {} - Body: {}", url, jsonBody);

        RequestBody body = RequestBody.create(
                jsonBody,
                MediaType.parse("application/json; charset=utf-8")
        );

        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(body)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json");

        // 添加认证头
        addAuthHeader(requestBuilder);

        return executeRequest(requestBuilder.build(), responseClass, endpoint);
    }

    /**
     * 同步 GET 请求
     *
     * @param endpoint      API 端点
     * @param responseClass 响应类型
     * @param <T>           响应数据类型
     * @return 响应数据
     * @throws ApiException 当 API 调用失败时
     */
    public <T> T get(String endpoint, Class<T> responseClass) {
        String url = buildUrl(endpoint);

        log.debug("API Request: GET {}", url);

        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .get()
                .header("Accept", "application/json");

        // 添加认证头
        addAuthHeader(requestBuilder);

        return executeRequest(requestBuilder.build(), responseClass, endpoint);
    }

    /**
     * 构建完整 URL
     *
     * @param endpoint API 端点
     * @return 完整 URL
     */
    private String buildUrl(String endpoint) {
        String baseUrl = properties.getBaseUrl();
        if (baseUrl.endsWith("/") && endpoint.startsWith("/")) {
            return baseUrl + endpoint.substring(1);
        } else if (!baseUrl.endsWith("/") && !endpoint.startsWith("/")) {
            return baseUrl + "/" + endpoint;
        }
        return baseUrl + endpoint;
    }

    /**
     * 添加认证头
     *
     * @param requestBuilder 请求构建器
     */
    private void addAuthHeader(Request.Builder requestBuilder) {
        String accessToken = properties.getAccessToken();
        if (accessToken != null && !accessToken.isEmpty()) {
            requestBuilder.header("Authorization", "Bearer " + accessToken);
        }
    }

    /**
     * 执行 HTTP 请求
     *
     * @param request       HTTP 请求
     * @param responseClass 响应类型
     * @param endpoint      API 端点（用于错误信息）
     * @param <T>           响应数据类型
     * @return 响应数据
     * @throws ApiException 当请求失败时
     */
    private <T> T executeRequest(Request request, Class<T> responseClass, String endpoint) {
        try (Response response = httpClient.newCall(request).execute()) {
            return parseResponse(response, responseClass, endpoint);
        } catch (IOException e) {
            log.error("API call failed: {} - {}", request.url(), e.getMessage());
            throw new ApiException("API call failed: " + e.getMessage(), e);
        }
    }

    /**
     * 解析响应
     *
     * @param response      HTTP 响应
     * @param responseClass 响应类型
     * @param endpoint      API 端点
     * @param <T>           响应数据类型
     * @return 响应数据
     * @throws ApiException 当响应解析失败或 API 返回错误时
     */
    private <T> T parseResponse(Response response, Class<T> responseClass, String endpoint) throws IOException {
        int httpStatus = response.code();

        if (!response.isSuccessful()) {
            String errorBody = response.body() != null ? response.body().string() : "";
            log.error("HTTP error: {} - {} - {}", httpStatus, endpoint, errorBody);
            throw new ApiException(
                    "HTTP error: " + httpStatus,
                    null,
                    httpStatus,
                    endpoint
            );
        }

        String responseBody = response.body() != null ? response.body().string() : "{}";
        log.debug("API Response: {}", responseBody);

        try {
            // 先解析为标准响应格式
            OneBotResponse<?> oneBotResponse = gson.fromJson(responseBody, OneBotResponse.class);

            if (oneBotResponse == null) {
                throw new ApiException("Empty response", null, httpStatus, endpoint);
            }

            // 检查返回码
            if (oneBotResponse.isFailed()) {
                String errorMsg = oneBotResponse.getErrorMessage();
                log.error("API error: {} - {} - retcode: {}", endpoint, errorMsg, oneBotResponse.getRetcode());
                throw new ApiException(
                        errorMsg != null ? errorMsg : "API error",
                        oneBotResponse.getRetcode(),
                        httpStatus,
                        endpoint
                );
            }

            // 解析数据部分
            if (oneBotResponse.getData() == null) {
                return null;
            }

            // 将 data 转换为 JSON 再解析为目标类型
            String dataJson = gson.toJson(oneBotResponse.getData());
            return gson.fromJson(dataJson, responseClass);

        } catch (JsonSyntaxException e) {
            log.error("Failed to parse response: {} - {}", endpoint, responseBody);
            throw new ApiException("Failed to parse response: " + e.getMessage(), e);
        }
    }

    /**
     * 带重试的 POST 请求
     *
     * @param endpoint      API 端点
     * @param requestBody   请求体
     * @param responseClass 响应类型
     * @param <T>           响应数据类型
     * @return 响应数据
     */
    public <T> T postWithRetry(String endpoint, Object requestBody, Class<T> responseClass) {
        if (!Boolean.TRUE.equals(properties.getRetryEnabled())) {
            return post(endpoint, requestBody, responseClass);
        }

        int maxRetries = properties.getMaxRetries();
        long delay = properties.getRetryDelay();
        double multiplier = properties.getRetryMultiplier();

        ApiException lastException = null;

        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                return post(endpoint, requestBody, responseClass);
            } catch (ApiException e) {
                lastException = e;

                // 如果是客户端错误（4xx），不 retry
                if (e.getHttpStatus() != null && e.getHttpStatus() >= 400 && e.getHttpStatus() < 500) {
                    throw e;
                }

                if (attempt < maxRetries) {
                    log.warn("Request failed (attempt {}/{}), retrying in {}ms: {}",
                            attempt + 1, maxRetries + 1, delay, endpoint);
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new ApiException("Retry interrupted", ie);
                    }
                    delay = (long) (delay * multiplier);
                }
            }
        }

        throw lastException;
    }
}
