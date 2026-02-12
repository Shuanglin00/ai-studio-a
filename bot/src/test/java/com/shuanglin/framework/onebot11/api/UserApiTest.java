package com.shuanglin.framework.onebot11.api;

import com.shuanglin.framework.onebot11.OneBot11TestBase;
import com.shuanglin.framework.onebot11.model.request.*;
import com.shuanglin.framework.onebot11.model.response.*;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserApi 单元测试
 *
 * @author Shuanglin
 * @since 1.0
 */
class UserApiTest extends OneBot11TestBase {

    private UserApi userApi;

    @BeforeEach
    void setUp() {
        userApi = new UserApi(apiClient);
    }

    @Test
    @DisplayName("获取好友列表 - 成功")
    void getFriendList_Success() throws InterruptedException {
        // Given
        String responseJson = createSuccessResponse("""
            [
                {"user_id": 111111, "nickname": "Friend1", "remark": "Remark1"},
                {"user_id": 222222, "nickname": "Friend2", "remark": "Remark2"}
            ]
            """);
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(responseJson));

        // When
        List<FriendInfoResponse> friends = userApi.getFriendList();

        // Then
        assertNotNull(friends);
        assertEquals(2, friends.size());
        assertEquals(111111L, friends.get(0).getUserId());
        assertEquals("Friend1", friends.get(0).getNickname());

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("/get_friend_list", recordedRequest.getPath());
    }

    @Test
    @DisplayName("获取陌生人信息 - 成功")
    void getStrangerInfo_Success() throws InterruptedException {
        // Given
        String responseJson = createSuccessResponse("""
            {"user_id": 111111, "nickname": "Stranger", "sex": "male", "age": 25}
            """);
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(responseJson));

        // When
        StrangerInfoResponse response = userApi.getStrangerInfo(111111L);

        // Then
        assertNotNull(response);
        assertEquals(111111L, response.getUserId());
        assertEquals("Stranger", response.getNickname());

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("/get_stranger_info", recordedRequest.getPath());
    }

    @Test
    @DisplayName("删除好友 - 成功")
    void deleteFriend_Success() throws InterruptedException {
        // Given
        String responseJson = createSuccessResponse("null");
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(responseJson));

        // When
        userApi.deleteFriend(111111L);

        // Then
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("/delete_friend", recordedRequest.getPath());
    }

    @Test
    @DisplayName("处理好友请求 - 同意")
    void handleFriendRequest_Approve() throws InterruptedException {
        // Given
        String responseJson = createSuccessResponse("null");
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(responseJson));

        HandleFriendRequestRequest request = HandleFriendRequestRequest.builder()
                .flag("request_flag_123")
                .approve(true)
                .remark("New Friend")
                .build();

        // When
        userApi.handleFriendRequest(request);

        // Then
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("/handle_friend_request", recordedRequest.getPath());
    }

    @Test
    @DisplayName("发送戳一戳 - 成功")
    void sendLike_Success() throws InterruptedException {
        // Given
        String responseJson = createSuccessResponse("null");
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(responseJson));

        // When
        userApi.sendLike(111111L, 5);

        // Then
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("/send_like", recordedRequest.getPath());
    }

    @Test
    @DisplayName("删除好友 - 参数校验失败")
    void deleteFriend_ValidationFailed() {
        // Given
        DeleteFriendRequest request = new DeleteFriendRequest(null);

        // When & Then
        assertThrows(Exception.class, () -> request.validate());
    }

    @Test
    @DisplayName("处理好友请求 - 参数校验失败")
    void handleFriendRequest_ValidationFailed() {
        // Given
        HandleFriendRequestRequest request = HandleFriendRequestRequest.builder()
                .flag(null)
                .build();

        // When & Then
        assertThrows(Exception.class, () -> request.validate());
    }

    @Test
    @DisplayName("发送戳一戳 - 参数校验失败")
    void sendLike_ValidationFailed() {
        // Given
        SendLikeRequest request = SendLikeRequest.builder()
                .userId(null)
                .times(5)
                .build();

        // When & Then
        assertThrows(Exception.class, () -> request.validate());
    }
}
