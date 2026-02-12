package com.shuanglin.framework.onebot11;

import com.shuanglin.framework.onebot11.client.ApiClient;
import com.shuanglin.framework.onebot11.config.OneBot11Properties;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

/**
 * OneBot 11 测试基类
 * 提供 MockWebServer 和 ApiClient 的初始化和清理
 *
 * @author Shuanglin
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
public abstract class OneBot11TestBase {

    protected MockWebServer mockWebServer;
    protected ApiClient apiClient;
    protected OneBot11Properties properties;

    /**
     * 测试前初始化
     *
     * @throws IOException 当 MockWebServer 启动失败时
     */
    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        properties = new OneBot11Properties();
        properties.setBaseUrl(mockWebServer.url("/").toString());
        properties.setAccessToken("test-token");
        properties.setRetryEnabled(false); // 测试中禁用重试

        OkHttpClient httpClient = new OkHttpClient.Builder().build();
        apiClient = new ApiClient(httpClient, properties);
    }

    /**
     * 测试后清理
     *
     * @throws IOException 当 MockWebServer 关闭失败时
     */
    @AfterEach
    void tearDown() throws IOException {
        if (mockWebServer != null) {
            mockWebServer.shutdown();
        }
    }

    /**
     * 获取 Mock Server URL
     *
     * @return Mock Server 的基础 URL
     */
    protected String getMockUrl() {
        return mockWebServer.url("/").toString();
    }

    /**
     * 创建成功的标准响应 JSON
     *
     * @param dataJson 数据部分的 JSON 字符串
     * @return 完整的响应 JSON
     */
    protected String createSuccessResponse(String dataJson) {
        return "{" +
                "\"status\": \"ok\"," +
                "\"retcode\": 0," +
                "\"data\": " + dataJson + "," +
                "\"message\": \"\"," +
                "\"wording\": \"\"" +
                "}";
    }

    /**
     * 创建失败的标准响应 JSON
     *
     * @param retcode  错误码
     * @param message  错误消息
     * @param wording  用户友好的错误消息
     * @return 完整的响应 JSON
     */
    protected String createErrorResponse(int retcode, String message, String wording) {
        return "{" +
                "\"status\": \"failed\"," +
                "\"retcode\": " + retcode + "," +
                "\"data\": null," +
                "\"message\": \"" + message + "\"," +
                "\"wording\": \"" + wording + "\"" +
                "}";
    }
}
