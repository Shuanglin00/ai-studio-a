package com.shuanglin.framework.onebot11.model.response;

import lombok.Data;

/**
 * 版本信息响应
 */
@Data
public class VersionInfoResponse {

    /**
     * 应用标识
     */
    private String appName;

    /**
     * 应用版本
     */
    private String appVersion;

    /**
     * OneBot 协议版本
     */
    private String protocolVersion;
}
