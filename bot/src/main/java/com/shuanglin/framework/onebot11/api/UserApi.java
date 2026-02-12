package com.shuanglin.framework.onebot11.api;

import com.shuanglin.framework.onebot11.client.ApiClient;
import com.shuanglin.framework.onebot11.model.request.*;
import com.shuanglin.framework.onebot11.model.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 用户相关 API
 * 包含好友管理、用户信息、处理好友申请等接口
 *
 * @author Shuanglin
 * @since 1.0
 */
@Slf4j
@RequiredArgsConstructor
public class UserApi {

    private final ApiClient apiClient;

    public List<FriendInfoResponse> getFriendList() {
        log.debug("Getting friend list");
        FriendInfoResponse[] array = apiClient.post("/get_friend_list", null, FriendInfoResponse[].class);
        return array != null ? List.of(array) : List.of();
    }

    public StrangerInfoResponse getStrangerInfo(Long userId) {
        return getStrangerInfo(userId, false);
    }

    public StrangerInfoResponse getStrangerInfo(Long userId, boolean noCache) {
        GetStrangerInfoRequest request = new GetStrangerInfoRequest(userId, noCache);
        log.debug("Getting stranger info: {}, noCache: {}", userId, noCache);
        return apiClient.post("/get_stranger_info", request, StrangerInfoResponse.class);
    }

    public void deleteFriend(Long userId) {
        DeleteFriendRequest request = new DeleteFriendRequest(userId);
        request.validate();
        log.debug("Deleting friend: {}", userId);
        apiClient.post("/delete_friend", request, Void.class);
    }

    public void handleFriendRequest(HandleFriendRequestRequest request) {
        request.validate();
        log.debug("Handling friend request: {} - approve: {}", request.getFlag(), request.getApprove());
        apiClient.post("/handle_friend_request", request, Void.class);
    }

    public void sendLike(Long userId) {
        sendLike(userId, 1);
    }

    public void sendLike(Long userId, Integer times) {
        SendLikeRequest request = SendLikeRequest.builder()
                .userId(userId)
                .times(times)
                .build();
        request.validate();
        log.debug("Sending like to user: {} times: {}", userId, times);
        apiClient.post("/send_like", request, Void.class);
    }
}
