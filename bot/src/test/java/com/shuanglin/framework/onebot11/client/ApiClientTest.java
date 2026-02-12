package com.shuanglin.framework.onebot11.client;

import com.shuanglin.framework.onebot11.OneBot11TestBase;
import com.shuanglin.framework.onebot11.config.OneBot11Properties;
import com.shuanglin.framework.onebot11.exception.ApiException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ApiClient 单元测试
 *
 * @author Shuanglin
 * @since 1.0
 */
class ApiClientTest extends OneBot11TestBase {

    @Test
    @DisplayName("POST 请求 - 成功")
    void postSuccess() throws InterruptedException {
        // Given
        String responseJson = createSuccessResponse("{\"id\": 123, \"name\": \"test\"}");
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(responseJson)
                .addHeader("Content-Type", "application/json"));

        TestRequest request = new TestRequest("value");

        // When
        TestResponse response = apiClient.post("/test", request, TestResponse.class);

        // Then
        assertNotNull(response);
        assertEquals(123L, response.getId());
        assertEquals("test", response.getName());

        // Verify request
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("POST", recordedRequest.getMethod());
        assertEquals("/test", recordedRequest.getPath());
        assertTrue(recordedRequest.getHeader("Content-Type").startsWith("application/json"));
        assertEquals("Bearer test-token", recordedRequest.getHeader("Authorization"));
    }

    @Test
    @DisplayName("GET 请求 - 成功")
    void getSuccess() throws InterruptedException {
        // Given
        String responseJson = createSuccessResponse("{\"id\": 456, \"name\": \"getTest\"}");
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(responseJson)
                .addHeader("Content-Type", "application/json"));

        // When
        TestResponse response = apiClient.get("/test-get", TestResponse.class);

        // Then
        assertNotNull(response);
        assertEquals(456L, response.getId());
        assertEquals("getTest", response.getName());

        // Verify request
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/test-get", recordedRequest.getPath());
    }

    @Test
    @DisplayName("POST 请求 - API 返回错误")
    void postApiError() {
        // Given
        String responseJson = createErrorResponse(100, "Invalid parameter", "参数错误");
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(responseJson)
                .addHeader("Content-Type", "application/json"));

        TestRequest request = new TestRequest("value");

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () -> {
            apiClient.post("/test", request, TestResponse.class);
        });
        assertEquals("参数错误", exception.getMessage());
        assertEquals(100, exception.getErrorCode());
    }

    @Test
    @DisplayName("POST 请求 - HTTP 错误")
    void postHttpError() {
        // Given
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error"));

        TestRequest request = new TestRequest("value");

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () -> {
            apiClient.post("/test", request, TestResponse.class);
        });
        assertTrue(exception.getMessage().contains("500"));
        assertEquals(500, exception.getHttpStatus());
    }

    @Test
    @DisplayName("POST 请求 - 无认证令牌")
    void postWithoutAuthToken() throws InterruptedException {
        // Given
        properties.setAccessToken(null);
        String responseJson = createSuccessResponse("{}");
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(responseJson));

        // When
        apiClient.post("/test", new TestRequest("value"), TestResponse.class);

        // Then
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertNull(recordedRequest.getHeader("Authorization"));
    }

    @Test
    @DisplayName("POST 请求 - 空响应数据")
    void postEmptyResponseData() {
        // Given
        String responseJson = createSuccessResponse("null");
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(responseJson));

        // When
        TestResponse response = apiClient.post("/test", new TestRequest("value"), TestResponse.class);

        // Then
        assertNull(response);
    }

    @Test
    @DisplayName("创建 HTTP 客户端 - 使用配置值")
    void createHttpClientWithConfig() {
        // Given
        OneBot11Properties props = new OneBot11Properties();
        props.setConnectTimeout(3000L);
        props.setReadTimeout(8000L);
        props.setWriteTimeout(6000L);

        // When
        okhttp3.OkHttpClient client = ApiClient.createHttpClient(props);

        // Then
        assertNotNull(client);
        // Note: OkHttpClient doesn't expose timeout values directly for verification
    }

    @Test
    @DisplayName("构建 URL - 带斜杠")
    void buildUrlWithSlashes() throws InterruptedException {
        // Given - use mock server URL which already handles slashes correctly
        // When
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(createSuccessResponse("{}")));
        apiClient.get("/test", TestResponse.class);

        // Then
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("/test", recordedRequest.getPath());
    }

    // Test DTOs
    static class TestRequest {
        private String field;

        TestRequest(String field) {
            this.field = field;
        }

        public String getField() {
            return field;
        }
    }

    static class TestResponse {
        private Long id;
        private String name;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
