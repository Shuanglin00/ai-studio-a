package com.shuanglin.framework.milky.model.request;
import lombok.AllArgsConstructor;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 发送群聊消息请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendGroupMessageRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 群ID
     */
    private Long groupId;

    /**
     * 消息内容
     */
    private List<Map<String, Object>> message;
}
