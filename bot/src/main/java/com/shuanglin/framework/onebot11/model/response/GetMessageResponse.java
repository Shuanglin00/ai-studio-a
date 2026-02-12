package com.shuanglin.framework.onebot11.model.response;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 获取消息详情响应
 */
@Data
public class GetMessageResponse {

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
     * 发送者信息
     */
    private Sender sender;

    /**
     * 消息内容（消息段数组）
     */
    private List<Map<String, Object>> message;

    /**
     * 原始消息内容字符串
     */
    @SerializedName("raw_message")
    private String rawMessage;

    /**
     * 发送时间戳
     */
    private Long time;

    /**
     * 发送者信息
     */
    @Data
    public static class Sender {
        /**
         * 发送者 QQ 号
         */
        @SerializedName("user_id")
        private Long userId;

        /**
         * 发送者昵称
         */
        private String nickname;

        /**
         * 群名片（群聊时）
         */
        private String card;

        /**
         * 群角色（群聊时）：owner, admin, member
         */
        private String role;

        /**
         * 专属头衔
         */
        private String title;
    }
}
