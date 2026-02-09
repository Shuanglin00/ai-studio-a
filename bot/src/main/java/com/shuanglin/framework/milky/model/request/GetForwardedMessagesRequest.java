package com.shuanglin.framework.milky.model.request;
import lombok.AllArgsConstructor;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 获取合并转发消息内容请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetForwardedMessagesRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 合并转发ID
     */
    private String forwardId;
}
