package com.shuanglin.framework.milky.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shuanglin.framework.milky.config.MilkyApiConfig;
import com.shuanglin.framework.milky.model.request.*;
import com.shuanglin.framework.milky.model.response.*;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Milky API 客户端
 */
@Slf4j
@Component
public class MilkyApiClient {

    private final MilkyApiConfig config;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public MilkyApiClient(MilkyApiConfig config, ObjectMapper objectMapper) {
        this.config = config;
        this.objectMapper = objectMapper;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(config.getConnectTimeout(), TimeUnit.MILLISECONDS)
                .readTimeout(config.getReadTimeout(), TimeUnit.MILLISECONDS)
                .writeTimeout(config.getTimeout(), TimeUnit.MILLISECONDS)
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request.Builder requestBuilder = original.newBuilder()
                            .header("Content-Type", "application/json")
                            .header("Accept", "application/json");
                    if (config.getAccessToken() != null && !config.getAccessToken().isEmpty()) {
                        requestBuilder.header("Authorization", "Bearer " + config.getAccessToken());
                    }
                    requestBuilder.method(original.method(), original.body());
                    return chain.proceed(requestBuilder.build());
                })
                .build();
    }

    private <T> T post(String apiPath, Object requestBody, Class<T> responseClass) throws IOException {
        String url = config.getBaseUrl() + apiPath;
        RequestBody body = RequestBody.create(
                objectMapper.writeValueAsString(requestBody),
                MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response.code());
            }

            String responseBody = response.body() != null ? response.body().string() : null;
            if (responseBody == null || responseBody.isEmpty()) {
                return null;
            }

            MilkyResponse<T> milkyResponse = objectMapper.readValue(responseBody,
                    objectMapper.getTypeFactory().constructParametricType(MilkyResponse.class, responseClass));

            if (!milkyResponse.isOk()) {
                throw new IOException("API returned error: " + milkyResponse.getMessage());
            }

            return milkyResponse.getData();
        }
    }

    private <T> T postEmptyBody(String apiPath, Class<T> responseClass) throws IOException {
        return post(apiPath, "{}", responseClass);
    }

    // ==================== 系统 API ====================

    /**
     * 获取登录信息
     */
    public LoginInfoResponse getLoginInfo() throws IOException {
        return postEmptyBody("/api/get_login_info", LoginInfoResponse.class);
    }

    /**
     * 获取协议端信息
     */
    public ImplInfoResponse getImplInfo() throws IOException {
        return postEmptyBody("/api/get_impl_info", ImplInfoResponse.class);
    }

    /**
     * 获取用户个人信息
     */
    public UserProfileResponse getUserProfile(Long userId) throws IOException {
        return post("/api/get_user_profile", new UserProfileRequest(userId), UserProfileResponse.class);
    }

    /**
     * 获取好友列表
     */
    public FriendListResponse getFriendList(Boolean noCache) throws IOException {
        return post("/api/get_friend_list", new FriendListRequest(noCache), FriendListResponse.class);
    }

    /**
     * 获取好友详情
     */
    public FriendDetailResponse getFriendInfo(Long userId, Boolean noCache) throws IOException {
        return post("/api/get_friend_info", new FriendInfoRequest(userId, noCache), FriendDetailResponse.class);
    }

    /**
     * 获取群列表
     */
    public GroupListResponse getGroupList(Boolean noCache) throws IOException {
        return post("/api/get_group_list", new GroupListRequest(noCache), GroupListResponse.class);
    }

    /**
     * 获取群信息
     */
    public GroupDetailResponse getGroupInfo(Long groupId, Boolean noCache) throws IOException {
        return post("/api/get_group_info", new GroupInfoRequest(groupId, noCache), GroupDetailResponse.class);
    }

    /**
     * 获取群成员列表
     */
    public GroupMemberListResponse getGroupMemberList(Long groupId, Boolean noCache) throws IOException {
        return post("/api/get_group_member_list", new GroupMemberListRequest(groupId, noCache), GroupMemberListResponse.class);
    }

    /**
     * 获取群成员信息
     */
    public GroupMemberDetailResponse getGroupMemberInfo(Long groupId, Long userId, Boolean noCache) throws IOException {
        return post("/api/get_group_member_info", new GroupMemberInfoRequest(groupId, userId, noCache), GroupMemberDetailResponse.class);
    }

    /**
     * 设置QQ账号头像
     */
    public Void setAvatar(String uri) throws IOException {
        post("/api/set_avatar", new SetAvatarRequest(uri), Void.class);
        return null;
    }

    /**
     * 设置QQ账号昵称
     */
    public Void setNickname(String newNickname) throws IOException {
        post("/api/set_nickname", new SetNicknameRequest(newNickname), Void.class);
        return null;
    }

    /**
     * 设置QQ账号个性签名
     */
    public Void setBio(String newBio) throws IOException {
        post("/api/set_bio", new SetBioRequest(newBio), Void.class);
        return null;
    }

    /**
     * 获取Cookies
     */
    public CookiesResponse getCookies(String domain) throws IOException {
        return post("/api/get_cookies", new GetCookiesRequest(domain), CookiesResponse.class);
    }

    /**
     * 获取CSRF Token
     */
    public CsrfTokenResponse getCsrfToken() throws IOException {
        return postEmptyBody("/api/get_csrf_token", CsrfTokenResponse.class);
    }

    // ==================== 消息 API ====================

    /**
     * 发送私聊消息
     */
    public MessageResponse sendPrivateMessage(Long userId, java.util.List<java.util.Map<String, Object>> message) throws IOException {
        return post("/api/send_private_message", new SendPrivateMessageRequest(userId, message), MessageResponse.class);
    }

    /**
     * 发送群聊消息
     */
    public MessageResponse sendGroupMessage(Long groupId, java.util.List<java.util.Map<String, Object>> message) throws IOException {
        return post("/api/send_group_message", new SendGroupMessageRequest(groupId, message), MessageResponse.class);
    }

    /**
     * 撤回私聊消息
     */
    public Void recallPrivateMessage(Long userId, Long messageSeq) throws IOException {
        post("/api/recall_private_message", new RecallPrivateMessageRequest(userId, messageSeq), Void.class);
        return null;
    }

    /**
     * 撤回群聊消息
     */
    public Void recallGroupMessage(Long groupId, Long messageSeq) throws IOException {
        post("/api/recall_group_message", new RecallGroupMessageRequest(groupId, messageSeq), Void.class);
        return null;
    }

    /**
     * 获取消息
     */
    public MessageDetailResponse getMessage(String messageScene, Long peerId, Long messageSeq) throws IOException {
        return post("/api/get_message", new GetMessageRequest(
                com.shuanglin.framework.milky.enums.MessageScene.fromValue(messageScene), peerId, messageSeq), MessageDetailResponse.class);
    }

    /**
     * 获取历史消息列表
     */
    public HistoryMessagesResponse getHistoryMessages(String messageScene, Long peerId, Long startMessageSeq, Integer limit) throws IOException {
        return post("/api/get_history_messages", new GetHistoryMessagesRequest(
                com.shuanglin.framework.milky.enums.MessageScene.fromValue(messageScene), peerId, startMessageSeq, limit), HistoryMessagesResponse.class);
    }

    /**
     * 获取临时资源链接
     */
    public ResourceTempUrlResponse getResourceTempUrl(String resourceId) throws IOException {
        return post("/api/get_resource_temp_url", new GetResourceTempUrlRequest(resourceId), ResourceTempUrlResponse.class);
    }

    /**
     * 获取合并转发消息内容
     */
    public ForwardedMessagesResponse getForwardedMessages(String forwardId) throws IOException {
        return post("/api/get_forwarded_messages", new GetForwardedMessagesRequest(forwardId), ForwardedMessagesResponse.class);
    }

    /**
     * 标记消息为已读
     */
    public Void markMessageAsRead(String messageScene, Long peerId, Long messageSeq) throws IOException {
        post("/api/mark_message_as_read", new MarkMessageAsReadRequest(
                com.shuanglin.framework.milky.enums.MessageScene.fromValue(messageScene), peerId, messageSeq), Void.class);
        return null;
    }

    // ==================== 好友 API ====================

    /**
     * 发送好友戳一戳
     */
    public NudgeResponse sendFriendNudge(Long userId, Boolean isSelf) throws IOException {
        return post("/api/send_friend_nudge", new SendFriendNudgeRequest(userId, isSelf), NudgeResponse.class);
    }

    /**
     * 获取好友列表请求（群成员）
     */
    public GroupMembersResponse getGroupMembers(Long groupId) throws IOException {
        return post("/api/get_group_members", new GetGroupMembersRequest(groupId), GroupMembersResponse.class);
    }

    /**
     * 获取陌生人信息
     */
    public StrangerResponse getStrangerInfo(Long userId, Boolean noCache) throws IOException {
        return post("/api/get_stranger_info", new GetStrangerInfoRequest(userId, noCache), StrangerResponse.class);
    }

    /**
     * 发送群戳一戳
     */
    public NudgeResponse sendGroupNudge(Long groupId, Long userId) throws IOException {
        return post("/api/send_group_nudge", new SendGroupNudgeRequest(groupId, userId), NudgeResponse.class);
    }

    // ==================== 群聊 API ====================

    /**
     * 获取群公告列表
     */
    public GroupAnnouncementListResponse getGroupAnnouncements(Long groupId) throws IOException {
        return post("/api/get_group_announcements", new GetGroupAnnouncementsRequest(groupId), GroupAnnouncementListResponse.class);
    }

    /**
     * 获取群荣誉信息
     */
    public GroupHonorResponse getGroupHonor(Long groupId, String honorType) throws IOException {
        return post("/api/get_group_honor", new GetGroupHonorRequest(groupId, honorType), GroupHonorResponse.class);
    }

    /**
     * 获取群整体信息
     */
    public GroupWholeResponse getGroupWholeInfo(Long groupId) throws IOException {
        return post("/api/get_group_whole_info", new GetGroupWholeInfoRequest(groupId), GroupWholeResponse.class);
    }

    /**
     * 设置群名称
     */
    public Void setGroupName(Long groupId, String groupName) throws IOException {
        post("/api/set_group_name", new SetGroupNameRequest(groupId, groupName), Void.class);
        return null;
    }

    /**
     * 设置群头像
     */
    public Void setGroupAvatar(Long groupId, String uri) throws IOException {
        post("/api/set_group_avatar", new SetGroupAvatarRequest(groupId, uri), Void.class);
        return null;
    }

    /**
     * 设置群公告
     */
    public Void setGroupAnnouncement(Long groupId, String content, Boolean isTop, Boolean confirmRequired, Boolean showEditInfo) throws IOException {
        post("/api/set_group_announcement", new SetGroupAnnouncementRequest(groupId, content, isTop, confirmRequired, showEditInfo), Void.class);
        return null;
    }

    /**
     * 设置群名片
     */
    public Void setGroupCard(Long groupId, Long userId, String newCard) throws IOException {
        post("/api/set_group_card", new SetGroupCardRequest(groupId, userId, newCard), Void.class);
        return null;
    }

    /**
     * 设置群专属头衔
     */
    public Void setGroupSpecialTitle(Long groupId, Long userId, String newSpecialTitle, Long duration) throws IOException {
        post("/api/set_group_special_title", new SetGroupSpecialTitleRequest(groupId, userId, newSpecialTitle, duration), Void.class);
        return null;
    }

    /**
     * 设置用户头衔
     */
    public Void setUserTitle(Long groupId, Long userId, String title) throws IOException {
        post("/api/set_user_title", new SetUserTitleRequest(groupId, userId, title), Void.class);
        return null;
    }

    /**
     * 设置群管理员
     */
    public Void setGroupAdmin(Long groupId, Long userId, Boolean enable) throws IOException {
        post("/api/set_group_admin", new SetGroupAdminRequest(groupId, userId, enable), Void.class);
        return null;
    }

    /**
     * 群成员禁言
     */
    public Void muteGroupMember(Long groupId, Long userId, Long duration) throws IOException {
        post("/api/mute_group_member", new MuteGroupMemberRequest(groupId, userId, duration), Void.class);
        return null;
    }

    /**
     * 全体禁言
     */
    public Void muteGroupAll(Long groupId, Boolean enable) throws IOException {
        post("/api/mute_group_all", new MuteGroupAllRequest(groupId, enable), Void.class);
        return null;
    }

    /**
     * 群成员踢出
     */
    public Void kickGroupMember(Long groupId, Long userId, Boolean rejectAddRequest) throws IOException {
        post("/api/kick_group_member", new KickGroupMemberRequest(groupId, userId, rejectAddRequest), Void.class);
        return null;
    }

    /**
     * 退出群
     */
    public Void leaveGroup(Long groupId) throws IOException {
        post("/api/leave_group", new LeaveGroupRequest(groupId), Void.class);
        return null;
    }

    /**
     * 邀请好友加群
     */
    public Void inviteFriendJoinGroup(Long groupId, Long[] userIds, Boolean invitee) throws IOException {
        post("/api/invite_friend_join_group", new InviteFriendJoinGroupRequest(groupId, userIds, invitee), Void.class);
        return null;
    }

    /**
     * 获取群邀请
     */
    public GroupAnnouncementListResponse getGroupInvitations(Long groupId) throws IOException {
        return post("/api/get_group_invitations", new GetGroupInvitationRequest(groupId), GroupAnnouncementListResponse.class);
    }

    // ==================== 文件 API ====================

    /**
     * 获取群文件列表
     */
    public FileListResponse getGroupFiles(Long groupId, String folderId, Integer pageStart, Integer pageNum, String sortBy, String order) throws IOException {
        return post("/api/get_group_files", new GetGroupFilesRequest(groupId, folderId, pageStart, pageNum, sortBy, order), FileListResponse.class);
    }

    /**
     * 获取群文件详情
     */
    public FileDetailResponse getGroupFileDetail(Long groupId, String fileId, String folderId) throws IOException {
        return post("/api/get_group_file_detail", new GetGroupFileDetailRequest(groupId, fileId, folderId), FileDetailResponse.class);
    }

    /**
     * 上传群文件
     */
    public FileUploadResponse uploadGroupFile(Long groupId, String folderId, String fileName, String fileType, String path) throws IOException {
        return post("/api/upload_group_file", new UploadGroupFileRequest(groupId, folderId, fileName, fileType, path), FileUploadResponse.class);
    }

    /**
     * 创建群文件目录
     */
    public CreateFolderResponse createGroupFolder(Long groupId, String parentId, String name) throws IOException {
        return post("/api/create_group_folder", new CreateGroupFolderRequest(groupId, parentId, name), CreateFolderResponse.class);
    }

    /**
     * 删除群文件
     */
    public Void deleteGroupFile(Long groupId, String fileId, String folderId) throws IOException {
        post("/api/delete_group_file", new DeleteGroupFileRequest(groupId, fileId, folderId), Void.class);
        return null;
    }

    /**
     * 删除群文件目录
     */
    public Void deleteGroupFolder(Long groupId, String folderId) throws IOException {
        post("/api/delete_group_folder", new DeleteGroupFolderRequest(groupId, folderId), Void.class);
        return null;
    }

    /**
     * 重命名群文件
     */
    public Void renameGroupFile(Long groupId, String fileId, String folderId, String newName) throws IOException {
        post("/api/rename_group_file", new RenameGroupFileRequest(groupId, fileId, folderId, newName), Void.class);
        return null;
    }

    /**
     * 移动群文件
     */
    public Void moveGroupFile(Long groupId, String fileId, String fromFolderId, String toFolderId) throws IOException {
        post("/api/move_group_file", new MoveGroupFileRequest(groupId, fileId, fromFolderId, toFolderId), Void.class);
        return null;
    }

    /**
     * 获取群文件下载地址
     */
    public FileDownloadUrlResponse getGroupFileDownloadUrl(Long groupId, String fileId, String folderId) throws IOException {
        return post("/api/get_group_file_download_url", new GetGroupFileDownloadUrlRequest(groupId, fileId, folderId), FileDownloadUrlResponse.class);
    }

    /**
     * 创建群链接
     */
    public CreateGroupLinkResponse createGroupLink(Long groupId, Integer type, Long expiration) throws IOException {
        return post("/api/create_group_link", new CreateGroupLinkRequest(groupId, type, expiration), CreateGroupLinkResponse.class);
    }

    /**
     * 上传群图片
     */
    public UploadGroupImageResponse uploadGroupImage(Long groupId, String type, String path) throws IOException {
        return post("/api/upload_group_image", new UploadGroupImageRequest(groupId, type, path), UploadGroupImageResponse.class);
    }
}
