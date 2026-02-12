package com.shuanglin.framework.onebot11.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.shuanglin.framework.onebot11.client.ApiClient;
import com.shuanglin.framework.onebot11.model.request.*;
import com.shuanglin.framework.onebot11.model.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 群组相关 API
 * 包含群管理、成员管理、禁言、踢人等接口
 *
 * @author Shuanglin
 * @since 1.0
 */
@Slf4j
@RequiredArgsConstructor
public class GroupApi {

    private final ApiClient apiClient;

    // ==================== 群信息查询 ====================

    /**
     * 获取群列表
     *
     * @return 群列表
     */
    public List<GroupInfoResponse> getGroupList() {
        log.debug("Getting group list");
        GroupInfoResponse[] array = apiClient.post("/get_group_list", null, GroupInfoResponse[].class);
        return array != null ? List.of(array) : List.of();
    }

    /**
     * 获取群信息
     *
     * @param groupId 群号
     * @return 群信息
     */
    public GroupInfoResponse getGroupInfo(Long groupId) {
        return getGroupInfo(groupId, false);
    }

    /**
     * 获取群信息
     *
     * @param groupId   群号
     * @param noCache 是否不使用缓存（使用缓存可能更新不及时，但响应更快）
     * @return 群信息
     */
    public GroupInfoResponse getGroupInfo(Long groupId, boolean noCache) {
        GetGroupInfoRequest request = new GetGroupInfoRequest(groupId, noCache);
        log.debug("Getting group info: {}, noCache: {}", groupId, noCache);
        return apiClient.post("/get_group_info", request, GroupInfoResponse.class);
    }

    /**
     * 获取群成员列表
     *
     * @param groupId 群号
     * @return 群成员列表
     */
    public List<GroupMemberInfoResponse> getGroupMemberList(Long groupId) {
        return getGroupMemberList(groupId, false);
    }

    /**
     * 获取群成员列表
     *
     * @param groupId   群号
     * @param noCache 是否不使用缓存
     * @return 群成员列表
     */
    public List<GroupMemberInfoResponse> getGroupMemberList(Long groupId, boolean noCache) {
        GetGroupMemberListRequest request = new GetGroupMemberListRequest(groupId, noCache);
        log.debug("Getting group member list: {}, noCache: {}", groupId, noCache);
        GroupMemberInfoResponse[] array = apiClient.post("/get_group_member_list", request, GroupMemberInfoResponse[].class);
        return array != null ? List.of(array) : List.of();
    }

    /**
     * 获取群成员信息
     *
     * @param groupId 群号
     * @param userId  QQ 号
     * @return 群成员信息
     */
    public GroupMemberInfoResponse getGroupMemberInfo(Long groupId, Long userId) {
        return getGroupMemberInfo(groupId, userId, false);
    }

    /**
     * 获取群成员信息
     *
     * @param groupId   群号
     * @param userId    QQ 号
     * @param noCache 是否不使用缓存
     * @return 群成员信息
     */
    public GroupMemberInfoResponse getGroupMemberInfo(Long groupId, Long userId, boolean noCache) {
        GetGroupMemberInfoRequest request = new GetGroupMemberInfoRequest(groupId, userId, noCache);
        log.debug("Getting group member info: group={}, user={}, noCache: {}", groupId, userId, noCache);
        return apiClient.post("/get_group_member_info", request, GroupMemberInfoResponse.class);
    }

    // ==================== 群成员管理 ====================

    /**
     * 群组踢人
     *
     * @param request 踢人请求
     */
    public void setGroupKick(SetGroupKickRequest request) {
        request.validate();
        log.debug("Kicking user {} from group {}", request.getUserId(), request.getGroupId());
        apiClient.post("/set_group_kick", request, Void.class);
    }

    /**
     * 群组踢人（快捷方法）
     *
     * @param groupId 群号
     * @param userId  要踢的 QQ 号
     */
    public void setGroupKick(Long groupId, Long userId) {
        setGroupKick(SetGroupKickRequest.builder()
                .groupId(groupId)
                .userId(userId)
                .build());
    }

    /**
     * 群组禁言
     *
     * @param request 禁言请求
     */
    public void setGroupBan(SetGroupBanRequest request) {
        request.validate();
        log.debug("Banning user {} in group {} for {} seconds",
                request.getUserId(), request.getGroupId(), request.getDuration());
        apiClient.post("/set_group_ban", request, Void.class);
    }

    /**
     * 群组禁言（快捷方法）
     *
     * @param groupId  群号
     * @param userId   要禁言的 QQ 号
     * @param duration 禁言时长（秒）
     */
    public void setGroupBan(Long groupId, Long userId, Integer duration) {
        setGroupBan(SetGroupBanRequest.builder()
                .groupId(groupId)
                .userId(userId)
                .duration(duration)
                .build());
    }

    /**
     * 解除群成员禁言
     *
     * @param groupId 群号
     * @param userId  QQ 号
     */
    public void liftGroupBan(Long groupId, Long userId) {
        setGroupBan(groupId, userId, 0);
    }

    /**
     * 群组全员禁言
     *
     * @param request 全员禁言请求
     */
    public void setGroupWholeBan(SetGroupWholeBanRequest request) {
        request.validate();
        log.debug("Setting whole ban for group {}: {}", request.getGroupId(), request.getEnable());
        apiClient.post("/set_group_whole_ban", request, Void.class);
    }

    /**
     * 群组全员禁言（快捷方法）
     *
     * @param groupId 群号
     * @param enable  是否开启全员禁言
     */
    public void setGroupWholeBan(Long groupId, Boolean enable) {
        setGroupWholeBan(SetGroupWholeBanRequest.builder()
                .groupId(groupId)
                .enable(enable)
                .build());
    }

    /**
     * 设置群管理员
     *
     * @param request 设置管理员请求
     */
    public void setGroupAdmin(SetGroupAdminRequest request) {
        request.validate();
        log.debug("Setting admin {} in group {}: {}",
                request.getUserId(), request.getGroupId(), request.getEnable());
        apiClient.post("/set_group_admin", request, Void.class);
    }

    /**
     * 设置群管理员（快捷方法）
     *
     * @param groupId 群号
     * @param userId  要设置的 QQ 号
     * @param enable  true 为设置，false 为取消
     */
    public void setGroupAdmin(Long groupId, Long userId, Boolean enable) {
        setGroupAdmin(SetGroupAdminRequest.builder()
                .groupId(groupId)
                .userId(userId)
                .enable(enable)
                .build());
    }

    /**
     * 设置群名片
     *
     * @param request 设置群名片请求
     */
    public void setGroupCard(SetGroupCardRequest request) {
        request.validate();
        log.debug("Setting card for user {} in group {}: {}",
                request.getUserId(), request.getGroupId(), request.getCard());
        apiClient.post("/set_group_card", request, Void.class);
    }

    /**
     * 设置群名片（快捷方法）
     *
     * @param groupId 群号
     * @param userId  要设置的 QQ 号
     * @param card    群名片内容
     */
    public void setGroupCard(Long groupId, Long userId, String card) {
        setGroupCard(SetGroupCardRequest.builder()
                .groupId(groupId)
                .userId(userId)
                .card(card)
                .build());
    }

    /**
     * 设置群名
     *
     * @param request 设置群名请求
     */
    public void setGroupName(SetGroupNameRequest request) {
        request.validate();
        log.debug("Setting group name for {}: {}", request.getGroupId(), request.getGroupName());
        apiClient.post("/set_group_name", request, Void.class);
    }

    /**
     * 设置群名（快捷方法）
     *
     * @param groupId   群号
     * @param groupName 新群名
     */
    public void setGroupName(Long groupId, String groupName) {
        setGroupName(SetGroupNameRequest.builder()
                .groupId(groupId)
                .groupName(groupName)
                .build());
    }

    /**
     * 退出群组
     *
     * @param request 退出群组请求
     */
    public void setGroupLeave(SetGroupLeaveRequest request) {
        request.validate();
        log.debug("Leaving group {} (isDismiss: {})", request.getGroupId(), request.getIsDismiss());
        apiClient.post("/set_group_leave", request, Void.class);
    }

    /**
     * 退出群组（快捷方法）
     *
     * @param groupId   群号
     * @param isDismiss 是否解散，如果登录号是群主则有效
     */
    public void setGroupLeave(Long groupId, Boolean isDismiss) {
        setGroupLeave(SetGroupLeaveRequest.builder()
                .groupId(groupId)
                .isDismiss(isDismiss)
                .build());
    }

    /**
     * 设置群组专属头衔
     *
     * @param request 设置专属头衔请求
     */
    public void setGroupSpecialTitle(SetGroupSpecialTitleRequest request) {
        request.validate();
        log.debug("Setting special title for user {} in group {}: {}",
                request.getUserId(), request.getGroupId(), request.getSpecialTitle());
        apiClient.post("/set_group_special_title", request, Void.class);
    }

    /**
     * 设置群组专属头衔（快捷方法）
     *
     * @param groupId      群号
     * @param userId       要设置的 QQ 号
     * @param specialTitle 专属头衔
     * @param duration     有效期（秒），-1 表示永久
     */
    public void setGroupSpecialTitle(Long groupId, Long userId, String specialTitle, Integer duration) {
        setGroupSpecialTitle(SetGroupSpecialTitleRequest.builder()
                .groupId(groupId)
                .userId(userId)
                .specialTitle(specialTitle)
                .duration(duration)
                .build());
    }
}
