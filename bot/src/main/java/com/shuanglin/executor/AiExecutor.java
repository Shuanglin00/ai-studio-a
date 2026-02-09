//package com.shuanglin.executor;
//
//import cn.hutool.core.util.IdUtil;
//import com.google.gson.Gson;
//import com.google.gson.JsonArray;
//import com.google.gson.JsonObject;
//import com.shuanglin.dao.GroupInfo;
//import com.shuanglin.dao.model.Model;
//import com.shuanglin.dao.model.ModelsRepository;
//import com.shuanglin.dao.SenderInfo;
//import com.shuanglin.executor.vo.ChatParam;
//import com.shuanglin.framework.annotation.BotCommand;
//import com.shuanglin.framework.annotation.GroupMessageHandler;
//import com.shuanglin.framework.bus.event.GroupMessageEvent;
//import com.shuanglin.utils.GroupInfoUtil;
//import com.shuanglin.utils.JsonUtils;
//import io.github.admin4j.http.util.HttpJsonUtil;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
///**
// * AI 执行器
// * 通过 HTTP 调用 AI 服务
// */
//@Service
//@Slf4j
//@RequiredArgsConstructor
//public class AiExecutor {
//
//	private final Gson gson;
//
//	private final GroupInfoUtil groupInfoUtil;
//
//	private final ModelsRepository modelsRepository;
//
//	@Value("${ai.service.base-url}")
//	private String aiServiceBaseUrl;
//
//	@BotCommand(role = "AI助手", description = "与AI进行对话")
//	@GroupMessageHandler(startWith = "#chat")
//	public void chat(GroupMessageEvent group) {
//		try {
//			// 1. 获取当前用户信息
//			SenderInfo senderInfo = groupInfoUtil.getGroupSenderInfo(group);
//			if (!groupInfoUtil.checkModelPermission(group, senderInfo.getModelInfo().getModelName())) {
//				return;
//			}
//
//			// 2. 构建请求参数
//			JsonObject memoryId = JsonUtils.flatten(
//				gson.toJsonTree(ChatParam.builder()
//					.senderInfo(senderInfo)
//					.groupMessageEvent(group)
//					.build())
//				.getAsJsonObject()
//			);
//
//			JsonObject requestBody = new JsonObject();
//			requestBody.add("memoryId", memoryId);
//			requestBody.addProperty("question", group.getMessage());
//
//			log.info("Calling AI service with request: {}", requestBody);
//
//			// 3. 调用 AI 服务
//			String answer = HttpJsonUtil.post(
//				aiServiceBaseUrl + "/api/ai/group-chat",
//				requestBody.toString(),
//				String.class
//			);
//
//			log.info("Received answer from AI service: {}", answer);
//
//			// 4. 发送回复消息
//			JsonObject data1 = new JsonObject();
//			data1.addProperty("text", answer);
//			JsonArray messages = new JsonArray();
//			JsonObject jsonObject1 = new JsonObject();
//			jsonObject1.addProperty("type", "text");
//			jsonObject1.add("data", data1);
//			messages.add(jsonObject1);
//			JsonObject body = new JsonObject();
//			body.add("message", messages);
//			body.addProperty("group_id", group.getGroupId());
//			HttpJsonUtil.post("http://127.0.0.1:3000/send_group_msg", body.toString());
//		} catch (Exception e) {
//			log.error("Error during chat: {}", e.getMessage(), e);
//		}
//	}
//
//	/**
//	 * 模型名称
//	 * 模型描述
//	 * 模型指令
//	 *
//	 * @param group
//	 */
//	@BotCommand(role = "AI助手", description = "发布新的AI模型", requireAdmin = true)
//	@GroupMessageHandler(startWith = "#发布模型")
//	public void publishModel(GroupMessageEvent group) {
//		String[] params = group.getMessage().split(" ");
//
//		//1. 获取当前用户信息;
//		Model model = new Model();
//		model.setModelName(params[0]);
//		model.setConstraints(params[1]);
//		model.setInstruction(params[2]);
//		model.setIsActive("true");
//		model.setId(IdUtil.getSnowflakeNextIdStr());
//		model.setConstraints("1. 你必须遵守中华人民共和国法律法规，不得逾越或触碰任何违法甚至损害中国形象。\n" +
//				"2. 你必须使用简体中文，或者繁体中文，或者粤语的俚语进行回去，取决于问题所使用语言。\n" +
//				"3. 你将扮演多个角色，回答符合角色设定且根据历史记录相关的回答。\n" +
//				"4. 回答内容尽可能符合角色设定，字数保持在200以内。");
//		groupInfoUtil.publishModel(model);
//		log.info("模型已发布-----");
//	}
//
//	@BotCommand(role = "AI助手", description = "切换使用的AI模型")
//	@GroupMessageHandler(startWith = "#选择模型")
//	public void switchModel(GroupMessageEvent group) {
//		String[] params = group.getMessage().split(" ");
//
//		//1. 获取当前用户信息;
//		SenderInfo senderInfo = groupInfoUtil.getGroupSenderInfo(group);
//
//		groupInfoUtil.switchModel(senderInfo, params[0]);
//	}
//
//}
