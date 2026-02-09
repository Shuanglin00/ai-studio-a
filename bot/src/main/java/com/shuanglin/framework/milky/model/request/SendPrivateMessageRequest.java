package com.shuanglin.framework.milky.model.request;
import lombok.AllArgsConstructor;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 发送私聊消息请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendPrivateMessageRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 消息内容
     */
    private List<Map<String, Object>> message;
}
