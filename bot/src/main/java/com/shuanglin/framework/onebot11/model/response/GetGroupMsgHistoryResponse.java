package com.shuanglin.framework.onebot11.model.response;

import lombok.Data;

import java.util.List;

/**
 * 获取群消息历史记录响应
 */
@Data
public class GetGroupMsgHistoryResponse {

    /**
     * 消息列表
     */
    private List<MessageInfo> messages;

    /**
     * 消息信息
     */
    @Data
    public static class MessageInfo {
        /**
         * 消息 ID
         */
        private Long messageId;

        /**
         * 消息真实 ID
         */
        private Long realId;

        /**
         * 发送者信息
         */
        private Sender sender;

        /**
         * 消息内容
         */
        private Object message;

        /**
         * 原始消息内容
         */
        private String rawMessage;

        /**
         * 发送时间戳
         */
        private Long time;
    }

    /**
     * 发送者信息
     */
    @Data
    public static class Sender {
        /**
         * 发送者 QQ 号
         */
        private Long userId;

        /**
         * 发送者昵称
         */
        private String nickname;

        /**
         * 群名片
         */
        private String card;

        /**
         * 群角色
         */
        private String role;
    }
}
