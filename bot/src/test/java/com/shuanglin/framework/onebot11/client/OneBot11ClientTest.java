package com.shuanglin.framework.onebot11.client;

import com.shuanglin.framework.onebot11.api.*;
import com.shuanglin.framework.onebot11.config.OneBot11Properties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OneBot11Client 单元测试
 *
 * @author Shuanglin
 * @since 1.0
 */
class OneBot11ClientTest {

    @Test
    @DisplayName("创建客户端 - 使用自定义配置")
    void createClientWithCustomConfig() {
        // Given
        OneBot11Properties properties = new OneBot11Properties();
        properties.setBaseUrl("http://localhost:8080");
        properties.setAccessToken("test-token");

        // When
        OneBot11Client client = new OneBot11Client(properties);

        // Then
        assertNotNull(client);
        assertEquals(properties, client.getProperties());
        assertNotNull(client.getApiClient());
        assertNotNull(client.message());
        assertNotNull(client.group());
        assertNotNull(client.user());
        assertNotNull(client.file());
        assertNotNull(client.system());
        assertNotNull(client.other());
    }

    @Test
    @DisplayName("创建客户端 - 使用默认配置")
    void createClientWithDefaultConfig() {
        // When
        OneBot11Client client = new OneBot11Client();

        // Then
        assertNotNull(client);
        assertNotNull(client.getProperties());
        assertEquals("http://127.0.0.1:3000", client.getProperties().getBaseUrl());
    }

    @Test
    @DisplayName("获取各 API 模块")
    void getApiModules() {
        // Given
        OneBot11Client client = new OneBot11Client();

        // Then
        MessageApi messageApi = client.message();
        assertNotNull(messageApi);
        assertSame(messageApi, client.getMessageApi());

        GroupApi groupApi = client.group();
        assertNotNull(groupApi);
        assertSame(groupApi, client.getGroupApi());

        UserApi userApi = client.user();
        assertNotNull(userApi);
        assertSame(userApi, client.getUserApi());

        FileApi fileApi = client.file();
        assertNotNull(fileApi);
        assertSame(fileApi, client.getFileApi());

        SystemApi systemApi = client.system();
        assertNotNull(systemApi);
        assertSame(systemApi, client.getSystemApi());

        OtherApi otherApi = client.other();
        assertNotNull(otherApi);
        assertSame(otherApi, client.getOtherApi());
    }

    @Test
    @DisplayName("API 模块一致性 - message() 返回相同实例")
    void apiModuleConsistency() {
        // Given
        OneBot11Client client = new OneBot11Client();

        // When
        MessageApi api1 = client.message();
        MessageApi api2 = client.message();

        // Then
        assertSame(api1, api2);
    }
}
