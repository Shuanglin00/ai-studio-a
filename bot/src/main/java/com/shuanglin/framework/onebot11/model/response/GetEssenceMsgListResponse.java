package com.shuanglin.framework.onebot11.model.response;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

/**
 * 获取精华消息列表响应
 * 注意：此 API 直接返回数组，不是包装对象
 */
@Data
public class GetEssenceMsgListResponse {

    /**
     * 精华消息列表
     */
    private List<EssenceMessage> data;

    /**
     * 精华消息
     */
    @Data
    public static class EssenceMessage {
        /**
         * 消息 ID
         */
        @SerializedName("message_id")
        private Long messageId;

        /**
         * 消息发送者 QQ 号
         */
        @SerializedName("sender_id")
        private Long senderId;

        /**
         * 消息发送者昵称
         */
        @SerializedName("sender_nick")
        private String senderNick;

        /**
         * 操作者 QQ 号（设置精华的人）
         */
        @SerializedName("operator_id")
        private Long operatorId;

        /**
         * 操作者昵称
         */
        @SerializedName("operator_nick")
        private String operatorNick;

        /**
         * 消息内容
         */
        private String content;

        /**
         * 设置时间戳
         */
        @SerializedName("set_time")
        private Long setTime;

        /**
         * 设置时间字符串
         */
        @SerializedName("set_time_for_read")
        private String setTimeForRead;
    }
}
