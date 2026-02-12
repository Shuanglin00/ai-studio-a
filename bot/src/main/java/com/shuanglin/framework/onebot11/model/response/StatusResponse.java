package com.shuanglin.framework.onebot11.model.response;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * 状态响应
 */
@Data
public class StatusResponse {

    /**
     * 是否在线
     */
    private Boolean online;

    /**
     * 状态是否正常
     */
    private Boolean good;

    /**
     * 运行统计
     */
    private Statistics stat;

    /**
     * 运行统计
     */
    @Data
    public static class Statistics {
        /**
         * 收到的消息数
         */
        @SerializedName("message_received")
        private Long messageReceived;

        /**
         * 发送的消息数
         */
        @SerializedName("message_sent")
        private Long messageSent;

        /**
         * 消息丢失数
         */
        @SerializedName("message_lost")
        private Long messageLost;

        /**
         * 断开连接次数
         */
        @SerializedName("disconnect_times")
        private Long disconnectTimes;

        /**
         * 丢失的连接数
         */
        @SerializedName("lost_times")
        private Long lostTimes;
    }
}
