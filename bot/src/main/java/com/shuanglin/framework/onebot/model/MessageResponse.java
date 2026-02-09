package com.shuanglin.framework.onebot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 消息发送响应
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageResponse {

    /**
     * 响应状态（ok/failed）
     */
    private String status;

    /**
     * 返回码
     */
    private Integer retcode;

    /**
     * 消息ID
     */
    private Long messageId;

    /**
     * 错误信息
     */
    private String message;

    /**
     * 是否成功
     */
    public boolean isSuccess() {
        return "ok".equals(status) || (retcode != null && retcode == 0);
    }
}
