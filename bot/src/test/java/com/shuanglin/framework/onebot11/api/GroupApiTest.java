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
 * GroupApi 单元测试
 *
 * @author Shuanglin
 * @since 1.0
 */
class GroupApiTest extends OneBot11TestBase {

    private GroupApi groupApi;

    @BeforeEach
    void setUp() {
        groupApi = new GroupApi(apiClient);
    }

    @Test
    @DisplayName("获取群列表 - 成功")
    void getGroupList_Success() throws InterruptedException {
        // Given
        String responseJson = createSuccessResponse("""
            [
                {"group_id": 123456, "group_name": "Test Group 1", "member_count": 100, "max_member_count": 500},
                {"group_id": 789012, "group_name": "Test Group 2", "member_count": 50, "max_member_count": 500}
            ]
            """);
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(responseJson));

        // When
        List<GroupInfoResponse> groups = groupApi.getGroupList();

        // Then
        assertNotNull(groups);
        assertEquals(2, groups.size());
        assertEquals(123456L, groups.get(0).getGroupId());
        assertEquals("Test Group 1", groups.get(0).getGroupName());

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("/get_group_list", recordedRequest.getPath());
    }

    @Test
    @DisplayName("获取群信息 - 成功")
    void getGroupInfo_Success() throws InterruptedException {
        // Given
        String responseJson = createSuccessResponse("""
            {"group_id": 123456, "group_name": "Test Group", "member_count": 100, "max_member_count": 500}
            """);
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(responseJson));

        // When
        GroupInfoResponse response = groupApi.getGroupInfo(123456L);

        // Then
        assertNotNull(response);
        assertEquals(123456L, response.getGroupId());
        assertEquals("Test Group", response.getGroupName());

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("/get_group_info", recordedRequest.getPath());
    }

    @Test
    @DisplayName("获取群成员列表 - 成功")
    void getGroupMemberList_Success() throws InterruptedException {
        // Given
        String responseJson = createSuccessResponse("""
            [
                {"group_id": 123456, "user_id": 111111, "nickname": "User1", "role": "owner"},
                {"group_id": 123456, "user_id": 222222, "nickname": "User2", "role": "admin"}
            ]
            """);
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(responseJson));

        // When
        List<GroupMemberInfoResponse> members = groupApi.getGroupMemberList(123456L);

        // Then
        assertNotNull(members);
        assertEquals(2, members.size());
        assertEquals(111111L, members.get(0).getUserId());
        assertEquals("owner", members.get(0).getRole());

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("/get_group_member_list", recordedRequest.getPath());
    }

    @Test
    @DisplayName("获取群成员信息 - 成功")
    void getGroupMemberInfo_Success() throws InterruptedException {
        // Given
        String responseJson = createSuccessResponse("""
            {"group_id": 123456, "user_id": 111111, "nickname": "User1", "role": "owner"}
            """);
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(responseJson));

        // When
        GroupMemberInfoResponse response = groupApi.getGroupMemberInfo(123456L, 111111L);

        // Then
        assertNotNull(response);
        assertEquals(111111L, response.getUserId());
        assertEquals("owner", response.getRole());

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("/get_group_member_info", recordedRequest.getPath());
    }

    @Test
    @DisplayName("群组踢人 - 成功")
    void setGroupKick_Success() throws InterruptedException {
        // Given
        String responseJson = createSuccessResponse("null");
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(responseJson));

        // When
        groupApi.setGroupKick(123456L, 111111L);

        // Then
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("/set_group_kick", recordedRequest.getPath());
    }

    @Test
    @DisplayName("群组禁言 - 成功")
    void setGroupBan_Success() throws InterruptedException {
        // Given
        String responseJson = createSuccessResponse("null");
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(responseJson));

        // When
        groupApi.setGroupBan(123456L, 111111L, 300);

        // Then
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("/set_group_ban", recordedRequest.getPath());
    }

    @Test
    @DisplayName("解除群成员禁言 - 成功")
    void liftGroupBan_Success() throws InterruptedException {
        // Given
        String responseJson = createSuccessResponse("null");
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(responseJson));

        // When
        groupApi.liftGroupBan(123456L, 111111L);

        // Then
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("/set_group_ban", recordedRequest.getPath());
    }

    @Test
    @DisplayName("群组全员禁言 - 成功")
    void setGroupWholeBan_Success() throws InterruptedException {
        // Given
        String responseJson = createSuccessResponse("null");
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(responseJson));

        // When
        groupApi.setGroupWholeBan(123456L, true);

        // Then
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("/set_group_whole_ban", recordedRequest.getPath());
    }

    @Test
    @DisplayName("设置群管理员 - 成功")
    void setGroupAdmin_Success() throws InterruptedException {
        // Given
        String responseJson = createSuccessResponse("null");
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(responseJson));

        // When
        groupApi.setGroupAdmin(123456L, 111111L, true);

        // Then
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("/set_group_admin", recordedRequest.getPath());
    }

    @Test
    @DisplayName("设置群名片 - 成功")
    void setGroupCard_Success() throws InterruptedException {
        // Given
        String responseJson = createSuccessResponse("null");
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(responseJson));

        // When
        groupApi.setGroupCard(123456L, 111111L, "New Card");

        // Then
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("/set_group_card", recordedRequest.getPath());
    }

    @Test
    @DisplayName("设置群名 - 成功")
    void setGroupName_Success() throws InterruptedException {
        // Given
        String responseJson = createSuccessResponse("null");
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(responseJson));

        // When
        groupApi.setGroupName(123456L, "New Group Name");

        // Then
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("/set_group_name", recordedRequest.getPath());
    }

    @Test
    @DisplayName("退出群组 - 成功")
    void setGroupLeave_Success() throws InterruptedException {
        // Given
        String responseJson = createSuccessResponse("null");
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(responseJson));

        // When
        groupApi.setGroupLeave(123456L, false);

        // Then
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("/set_group_leave", recordedRequest.getPath());
    }

    @Test
    @DisplayName("设置群组专属头衔 - 成功")
    void setGroupSpecialTitle_Success() throws InterruptedException {
        // Given
        String responseJson = createSuccessResponse("null");
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(responseJson));

        // When
        groupApi.setGroupSpecialTitle(123456L, 111111L, "VIP", -1);

        // Then
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("/set_group_special_title", recordedRequest.getPath());
    }

    @Test
    @DisplayName("群组踢人 - 参数校验失败")
    void setGroupKick_ValidationFailed() {
        // Given
        SetGroupKickRequest request = SetGroupKickRequest.builder()
                .groupId(null)
                .userId(111111L)
                .build();

        // When & Then
        assertThrows(Exception.class, () -> groupApi.setGroupKick(request));
    }

    @Test
    @DisplayName("群组禁言 - 参数校验失败")
    void setGroupBan_ValidationFailed() {
        // Given
        SetGroupBanRequest request = SetGroupBanRequest.builder()
                .groupId(123456L)
                .userId(null)
                .duration(300)
                .build();

        // When & Then
        assertThrows(Exception.class, () -> groupApi.setGroupBan(request));
    }
}
