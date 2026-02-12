package com.shuanglin.framework.onebot11.client;

import com.shuanglin.framework.onebot11.api.*;
import com.shuanglin.framework.onebot11.config.OneBot11Properties;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;

/**
 * OneBot 11 客户端主类
 * 提供所有 API 的入口，使用外观模式统一访问
 *
 * 使用示例:
 * <pre>
 * OneBot11Client client = new OneBot11Client(properties);
 * // 发送群消息
 * client.message().sendGroupMessage(request);
 * // 获取群列表
 * client.group().getGroupList();
 * </pre>
 *
 * @author Shuanglin
 * @since 1.0
 */
@Slf4j
public class OneBot11Client {

    @Getter
    private final MessageApi messageApi;

    @Getter
    private final GroupApi groupApi;

    @Getter
    private final UserApi userApi;

    @Getter
    private final FileApi fileApi;

    @Getter
    private final SystemApi systemApi;

    @Getter
    private final OtherApi otherApi;

    private final OneBot11Properties properties;
    private final ApiClient apiClient;

    /**
     * 创建 OneBot 11 客户端
     *
     * @param properties 配置属性
     */
    public OneBot11Client(OneBot11Properties properties) {
        this.properties = properties;
        OkHttpClient httpClient = ApiClient.createHttpClient(properties);
        this.apiClient = new ApiClient(httpClient, properties);

        // 初始化各 API 模块
        this.messageApi = new MessageApi(apiClient);
        this.groupApi = new GroupApi(apiClient);
        this.userApi = new UserApi(apiClient);
        this.fileApi = new FileApi(apiClient);
        this.systemApi = new SystemApi(apiClient);
        this.otherApi = new OtherApi(apiClient);

        log.info("OneBot11Client initialized with baseUrl: {}", properties.getBaseUrl());
    }

    /**
     * 创建 OneBot 11 客户端（使用默认配置）
     */
    public OneBot11Client() {
        this(new OneBot11Properties());
    }

    /**
     * 获取消息相关 API
     *
     * @return MessageApi 实例
     */
    public MessageApi message() {
        return messageApi;
    }

    /**
     * 获取群组相关 API
     *
     * @return GroupApi 实例
     */
    public GroupApi group() {
        return groupApi;
    }

    /**
     * 获取用户相关 API
     *
     * @return UserApi 实例
     */
    public UserApi user() {
        return userApi;
    }

    /**
     * 获取文件相关 API
     *
     * @return FileApi 实例
     */
    public FileApi file() {
        return fileApi;
    }

    /**
     * 获取系统相关 API
     *
     * @return SystemApi 实例
     */
    public SystemApi system() {
        return systemApi;
    }

    /**
     * 获取其他 API
     *
     * @return OtherApi 实例
     */
    public OtherApi other() {
        return otherApi;
    }

    /**
     * 获取配置属性
     *
     * @return 配置属性
     */
    public OneBot11Properties getProperties() {
        return properties;
    }

    /**
     * 获取底层 API 客户端
     *
     * @return ApiClient 实例
     */
    public ApiClient getApiClient() {
        return apiClient;
    }
}
