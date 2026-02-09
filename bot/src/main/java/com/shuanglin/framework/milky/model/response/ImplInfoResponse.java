package com.shuanglin.framework.milky.model.response;

import com.shuanglin.framework.milky.enums.QqProtocolType;
import lombok.Data;

import java.io.Serializable;

/**
 * 协议端信息响应
 */
@Data
public class ImplInfoResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 实现名称
     */
    private String implName;

    /**
     * 实现版本
     */
    private String implVersion;

    /**
     * QQ协议版本
     */
    private String qqProtocolVersion;

    /**
     * QQ协议类型
     */
    private QqProtocolType qqProtocolType;

    /**
     * Milky协议版本
     */
    private String milkyVersion;
}
