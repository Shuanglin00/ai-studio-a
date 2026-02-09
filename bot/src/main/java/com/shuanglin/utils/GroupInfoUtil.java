package com.shuanglin.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shuanglin.dao.bot.UserSession;
import com.shuanglin.dao.model.Model;
import com.shuanglin.dao.GroupInfo;
import com.shuanglin.dao.model.ModelInfo;
import com.shuanglin.dao.model.ModelsRepository;
import com.shuanglin.dao.SenderInfo;
import com.shuanglin.framework.bus.event.GroupMessageEvent;
import com.shuanglin.framework.session.SessionManager;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * GroupInfo工具类（已废弃）
 *
 * @deprecated 自2.0版本起废弃，计划3个版本后移除。
 *             请使用以下新组件替代：
 *             - {@link GroupConfigManager} 替代 getGroupInfo() 和 checkModelPermission()
 *             - {@link SessionManager} 替代 getGroupSenderInfo() 和 switchModel()
 *             - {@link com.shuanglin.framework.role.RoleManager} 替代 publishModel()
 */
@Deprecated(since = "2.0", forRemoval = true)
@Component
@Slf4j
public class GroupInfoUtil {
	private final static String GROUP_SENDER_STAFF = "group_sender_staff_";

	private final static String GROUP_INFO_STAFF = "group_info_staff_";

	@Resource(name = "senderInfoRedisTemplate")
	private RedisTemplate<String, Map<String, SenderInfo>> senderInfoRedisTemplate;

	@Qualifier("groupInfoRedisTemplate")
	@Resource(name = "groupInfoRedisTemplate")
	private RedisTemplate<String, Object> groupInfoRedisTemplate;

	private final ModelsRepository modelsRepository;

	// 新组件（适配器模式）
	private final SessionManager sessionManager;

	@Autowired
	public GroupInfoUtil(
			ModelsRepository modelsRepository,
			SessionManager sessionManager) {
		this.modelsRepository = modelsRepository;
		this.sessionManager = sessionManager;
	}

	/**
	 * 发布模型
	 * 
	 * @deprecated 请使用 {@link com.shuanglin.framework.role.RoleManager#createRole}
	 */
	@Deprecated
	public void publishModel(Model model) {
		Model modelByModelName = modelsRepository.getModelByModelName(model.getModelName());
		if (modelByModelName != null) {
			log.info("已存在模型");
			return;
		}
		modelsRepository.save(model);
		senderInfoRedisTemplate.delete(GROUP_SENDER_STAFF);
		groupInfoRedisTemplate.delete(GROUP_INFO_STAFF);
	}

	/**
	 * 切换模型
	 * 
	 * @deprecated 请使用 {@link SessionManager#updateUserModel}
	 */
	@Deprecated
	public void switchModel(SenderInfo senderInfo, String modelName) {
		// 委托给SessionManager
		sessionManager.updateUserModel(senderInfo.getGroupId(), 
				senderInfo.getUserId(), modelName);
		log.warn("GroupInfoUtil.switchModel is deprecated, use SessionManager.updateUserModel instead");
		
		// 保留原有逻辑以兼容
		Model modelByModelName = modelsRepository.getModelByModelName(modelName);
		if (modelByModelName != null) {
			Map<String, SenderInfo> senderInfoMap = senderInfoRedisTemplate.opsForValue().get(GROUP_SENDER_STAFF + senderInfo.getGroupId());
			ModelInfo modelInfo = senderInfo.getModelInfo();
			modelInfo.setModelName(modelByModelName.getModelName());
			senderInfoMap.put(senderInfo.getUserId(), senderInfo);
			senderInfoMap.remove(null);
			senderInfoRedisTemplate.opsForValue().set(GROUP_SENDER_STAFF + senderInfo.getGroupId(), senderInfoMap);
		}
	}

	/**
	 * 检查本群是否开启模型
	 *
	 * @deprecated 请使用相关权限校验方法
	 */
	@Deprecated
	public boolean checkModelPermission(GroupMessageEvent groupMessageEvent, String selectModel) {
		log.warn("GroupInfoUtil.checkModelPermission is deprecated, use PermissionValidator instead");
		
		// 保留原有逻辑以兼容
		GroupInfo groupInfo = getGroupInfo(groupMessageEvent);
		List<String> groupActiveModels = groupInfo.getModelInfo().getActiveModels();
		return groupActiveModels.contains(selectModel);
	}

	/**
	 * 获取群聊发送者信息
	 * 
	 * @deprecated 请使用 {@link SessionManager#getUserSession}
	 */
	@Deprecated
	public SenderInfo getGroupSenderInfo(GroupMessageEvent groupMessageEvent) {
		log.warn("GroupInfoUtil.getGroupSenderInfo is deprecated, use SessionManager.getUserSession instead");

		// 保留原有逻辑以兼容
		Map<String, SenderInfo> senderInfoMap = senderInfoRedisTemplate.opsForValue().get(GROUP_SENDER_STAFF + groupMessageEvent.getGroupId());
		if (senderInfoMap == null) {
			senderInfoMap = new HashMap<>();
			SenderInfo.getInstance().setGroupId(groupMessageEvent.getGroupId());
			senderInfoMap.put(groupMessageEvent.getGroupId(), SenderInfo.getInstance());
			senderInfoRedisTemplate.opsForValue().set(GROUP_SENDER_STAFF + groupMessageEvent.getGroupId(), senderInfoMap);
			return SenderInfo.getInstance();
		}
		if (senderInfoMap.get(String.valueOf(groupMessageEvent.getUserId())) == null) {
			List<Model> actives = modelsRepository.getModelsByIsActive("true");
			SenderInfo.getInstance().setGroupId(groupMessageEvent.getGroupId());
			SenderInfo.getInstance().setUserId(String.valueOf(groupMessageEvent.getUserId()));
			SenderInfo.getInstance().setModelInfo(ModelInfo.builder()
					.activeModels(actives.stream().map(Model::getModelName).collect(Collectors.toList()))
					.modelName(actives.get(0).getModelName())
					.build());
			senderInfoMap.put(String.valueOf(groupMessageEvent.getUserId()), SenderInfo.getInstance());
			senderInfoMap.remove(null);
			senderInfoRedisTemplate.opsForValue().set(GROUP_SENDER_STAFF + groupMessageEvent.getGroupId(), senderInfoMap);
			return SenderInfo.getInstance();
		}
		return senderInfoMap.get(String.valueOf(groupMessageEvent.getUserId()));
	}

	/**
	 * 获取群聊信息
	 * 
	 * @deprecated 请使用 {@link GroupConfigManager#getGroupConfig}
	 */
	@Deprecated
	public GroupInfo getGroupInfo(GroupMessageEvent groupMessageEvent) {
		log.warn("GroupInfoUtil.getGroupInfo is deprecated, use GroupConfigManager.getGroupConfig instead");

		// 保留原有逻辑以兼容
		GroupInfo groupInfo = new ObjectMapper().convertValue(groupInfoRedisTemplate.opsForHash().get(GROUP_INFO_STAFF, groupMessageEvent.getGroupId()), GroupInfo.class);
		if (groupInfo == null) {
			List<String> modelsByActive = modelsRepository.getModelsByIsActive("true").stream().map(Model::getModelName).collect(Collectors.toList());
			groupInfo = GroupInfo.getInstance();
			groupInfo.setGroupId(groupMessageEvent.getGroupId());
			groupInfo.setModelInfo(ModelInfo.builder().activeModels(modelsByActive).modelName(modelsByActive.isEmpty() ? "1" : modelsByActive.get(0)).build());
			GroupInfo.getInstance().setGroupId(groupMessageEvent.getGroupId());
			groupInfoRedisTemplate.opsForHash().put(GROUP_INFO_STAFF, groupMessageEvent.getGroupId(), GroupInfo.getInstance());
		}
		return groupInfo;
	}

}
