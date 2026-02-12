package com.shuanglin.framework.onebot11.api;

import com.shuanglin.framework.onebot11.client.ApiClient;
import com.shuanglin.framework.onebot11.model.request.SetOnlineStatusRequest;
import com.shuanglin.framework.onebot11.model.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 系统相关 API
 * 包含登录信息、版本信息、状态、Cookies 等接口
 *
 * @author Shuanglin
 * @since 1.0
 */
@Slf4j
@RequiredArgsConstructor
public class SystemApi {

    private final ApiClient apiClient;

    /**
     * 获取登录号信息
     *
     * @return 登录号信息
     */
    public LoginInfoResponse getLoginInfo() {
        log.debug("Getting login info");
        return apiClient.get("/get_login_info", LoginInfoResponse.class);
    }

    /**
     * 获取版本信息
     *
     * @return 版本信息
     */
    public VersionInfoResponse getVersionInfo() {
        log.debug("Getting version info");
        return apiClient.get("/get_version_info", VersionInfoResponse.class);
    }

    /**
     * 获取状态
     *
     * @return 状态信息
     */
    public StatusResponse getStatus() {
        log.debug("Getting status");
        return apiClient.get("/get_status", StatusResponse.class);
    }

    /**
     * 获取 Cookies
     *
     * @param domain 域名
     * @return Cookies 信息
     */
    public CookiesResponse getCookies(String domain) {
        log.debug("Getting cookies for domain: {}", domain);
        return apiClient.post("/get_cookies", new DomainRequest(domain), CookiesResponse.class);
    }

    /**
     * 获取 CSRF Token
     *
     * @return CSRF Token
     */
    public CsrfTokenResponse getCsrfToken() {
        log.debug("Getting CSRF token");
        return apiClient.get("/get_csrf_token", CsrfTokenResponse.class);
    }

    /**
     * 设置在线状态
     *
     * @param status 状态：online/away/hidden/busy
     */
    public void setOnlineStatus(String status) {
        SetOnlineStatusRequest request = SetOnlineStatusRequest.builder()
                .status(status)
                .build();
        request.validate();
        log.debug("Setting online status to: {}", status);
        apiClient.post("/set_online_status", request, Void.class);
    }

    /**
     * 清理缓存
     */
    public void cleanCache() {
        log.debug("Cleaning cache");
        apiClient.post("/clean_cache", "{}", Void.class);
    }

    // 内部请求类
    private record DomainRequest(String domain) {}
}
