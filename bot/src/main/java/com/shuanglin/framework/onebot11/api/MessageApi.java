package com.shuanglin.framework.onebot11.api;

import com.shuanglin.framework.onebot11.client.ApiClient;
import com.shuanglin.framework.onebot11.model.request.*;
import com.shuanglin.framework.onebot11.model.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 消息相关 API
 * 包含发送消息、获取消息、撤回消息等接口
 *
 * @author Shuanglin
 * @since 1.0
 */
@Slf4j
@RequiredArgsConstructor
public class MessageApi {

    private final ApiClient apiClient;

    /**
     * 发送私聊消息
     *
     * @param request 发送私聊消息请求
     * @return 发送结果
     */
    public SendMessageResponse sendPrivateMessage(SendPrivateMessageRequest request) {
        request.validate();
        log.debug("Sending private message to user: {}", request.getUserId());
        return apiClient.post("/send_private_msg", request, SendMessageResponse.class);
    }

    /**
     * 发送群消息
     *
     * @param request 发送群消息请求
     * @return 发送结果
     */
    public SendMessageResponse sendGroupMessage(SendGroupMessageRequest request) {
        request.validate();
        log.debug("Sending group message to group: {}", request.getGroupId());
        return apiClient.post("/send_group_msg", request, SendMessageResponse.class);
    }

    /**
     * 发送消息（通用接口）
     *
     * @param request 发送消息请求
     * @return 发送结果
     */
    public SendMessageResponse sendMessage(SendMessageRequest request) {
        request.validate();
        log.debug("Sending message, type: {}", request.getMessageType());
        return apiClient.post("/send_msg", request, SendMessageResponse.class);
    }

    /**
     * 获取消息详情
     *
     * @param messageId 消息 ID
     * @return 消息详情
     */
    public GetMessageResponse getMessage(Long messageId) {
        GetMessageRequest request = new GetMessageRequest(messageId);
        request.validate();
        log.debug("Getting message details: {}", messageId);
        return apiClient.post("/get_msg", request, GetMessageResponse.class);
    }

    /**
     * 撤回消息
     *
     * @param messageId 消息 ID
     */
    public void deleteMessage(Long messageId) {
        DeleteMessageRequest request = new DeleteMessageRequest(messageId);
        request.validate();
        log.debug("Deleting message: {}", messageId);
        apiClient.post("/delete_msg", request, Void.class);
    }

    /**
     * 获取合并转发消息内容
     *
     * @param messageId 合并转发消息 ID
     * @return 转发消息内容
     */
    public GetForwardMessageResponse getForwardMessage(String messageId) {
        GetForwardMessageRequest request = new GetForwardMessageRequest(messageId);
        request.validate();
        log.debug("Getting forward message: {}", messageId);
        return apiClient.post("/get_forward_msg", request, GetForwardMessageResponse.class);
    }

    /**
     * 发送合并转发消息（群聊）
     *
     * @param request 发送合并转发消息请求
     * @return 发送结果
     */
    public SendMessageResponse sendGroupForwardMessage(SendGroupForwardMessageRequest request) {
        request.validate();
        log.debug("Sending group forward message to group: {}", request.getGroupId());
        return apiClient.post("/send_group_forward_msg", request, SendMessageResponse.class);
    }

    /**
     * 发送合并转发消息（私聊）
     *
     * @param request 发送合并转发消息请求
     * @return 发送结果
     */
    public SendMessageResponse sendPrivateForwardMessage(SendPrivateForwardMessageRequest request) {
        request.validate();
        log.debug("Sending private forward message to user: {}", request.getUserId());
        return apiClient.post("/send_private_forward_msg", request, SendMessageResponse.class);
    }

    /**
     * 获取群消息历史记录
     *
     * @param request 获取历史消息请求
     * @return 历史消息列表
     */
    public GetGroupMsgHistoryResponse getGroupMsgHistory(GetGroupMsgHistoryRequest request) {
        request.validate();
        log.debug("Getting group message history for group: {}", request.getGroupId());
        return apiClient.post("/get_group_msg_history", request, GetGroupMsgHistoryResponse.class);
    }

    /**
     * 设置群精华消息
     *
     * @param messageId 消息 ID
     */
    public void setEssenceMessage(Long messageId) {
        SetEssenceMessageRequest request = new SetEssenceMessageRequest(messageId);
        request.validate();
        log.debug("Setting essence message: {}", messageId);
        apiClient.post("/set_essence_msg", request, Void.class);
    }

    /**
     * 移除群精华消息
     *
     * @param messageId 消息 ID
     */
    public void deleteEssenceMessage(Long messageId) {
        DeleteEssenceMessageRequest request = new DeleteEssenceMessageRequest(messageId);
        request.validate();
        log.debug("Deleting essence message: {}", messageId);
        apiClient.post("/delete_essence_msg", request, Void.class);
    }

    /**
     * 获取精华消息列表
     *
     * @param groupId 群号
     * @return 精华消息列表
     */
    public GetEssenceMsgListResponse getEssenceMsgList(Long groupId) {
        GetEssenceMsgListRequest request = new GetEssenceMsgListRequest(groupId);
        request.validate();
        log.debug("Getting essence message list for group: {}", groupId);
        return apiClient.post("/get_essence_msg_list", request, GetEssenceMsgListResponse.class);
    }

    // ==================== 快捷方法 ====================

    /**
     * 快速发送私聊消息（纯文本）
     *
     * @param userId  对方 QQ 号
     * @param message 消息内容
     * @return 发送结果
     */
    public SendMessageResponse sendPrivateMessage(Long userId, String message) {
        return sendPrivateMessage(SendPrivateMessageRequest.builder()
                .userId(userId)
                .message(message)
                .build());
    }

    /**
     * 快速发送群消息（纯文本）
     *
     * @param groupId 群号
     * @param message 消息内容
     * @return 发送结果
     */
    public SendMessageResponse sendGroupMessage(Long groupId, String message) {
        return sendGroupMessage(SendGroupMessageRequest.builder()
                .groupId(groupId)
                .message(message)
                .build());
    }
}
