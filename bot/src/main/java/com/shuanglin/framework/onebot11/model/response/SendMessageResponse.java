package com.shuanglin.framework.onebot11.model.response;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * 发送消息响应
 */
@Data
public class SendMessageResponse {

    /**
     * 消息 ID
     */
    @SerializedName("message_id")
    private Long messageId;

    /**
     * 消息真实 ID
     */
    @SerializedName("real_id")
    private Long realId;

    /**
     * 发送时间戳
     */
    private Long time;
}
