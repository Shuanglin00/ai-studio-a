package com.shuanglin.framework.onebot11.model.response;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 获取合并转发消息内容响应
 */
@Data
public class GetForwardMessageResponse {

    /**
     * 转发消息内容
     */
    private List<MessageNode> message;

    /**
     * 消息节点
     */
    @Data
    public static class MessageNode {
        /**
         * 节点类型（node）
         */
        private String type;

        /**
         * 节点数据
         */
        private NodeData data;
    }

    /**
     * 节点数据
     */
    @Data
    public static class NodeData {
        /**
         * 发送者 QQ 号
         */
        private Long uin;

        /**
         * 发送者昵称
         */
        private String name;

        /**
         * 消息内容
         */
        private List<Map<String, Object>> content;

        /**
         * 发送时间戳
         */
        private Long time;
    }
}
