package com.shuanglin.framework.listener;

import com.google.gson.Gson;
import com.shuanglin.framework.bus.MessageBus;
import com.shuanglin.framework.bus.event.Event;
import com.shuanglin.framework.bus.event.GroupMessageEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GroupMessageListener 集成测试
 */
@ExtendWith(MockitoExtension.class)
class GroupMessageListenerTest {

    private Gson gson = new Gson();

    @Test
    @DisplayName("测试消息解析逻辑")
    void testParseEventLogic() {
        // Logger log = LoggerFactory.getLogger(PublishToBusAspect.class);

        String jsonStr = "{\"self_id\":2784152733,\"user_id\":1751649231,\"message_type\":\"group\"," +
                "\"group_id\":345693826,\"raw_message\":\"test\",\"post_type\":\"message\"}";

        // 模拟 parseEvent 逻辑
        com.google.gson.JsonObject jsonObj = com.google.gson.JsonParser.parseString(jsonStr).getAsJsonObject();

        String postType = jsonObj.has("post_type") ? jsonObj.get("post_type").getAsString() : "";
        String messageType = jsonObj.has("message_type") ? jsonObj.get("message_type").getAsString() : "";

        System.out.println("post_type=" + postType + ", message_type=" + messageType);

        Class<? extends Event> eventClass;
        if ("message".equals(postType) && "group".equals(messageType)) {
            eventClass = GroupMessageEvent.class;
            System.out.println("Using GroupMessageEvent.class");
        } else {
            eventClass = Event.class;
            System.out.println("Using Event.class");
        }

        Event event = gson.fromJson(jsonStr, eventClass);
        System.out.println("Event class after fromJson: " + event.getClass().getName());
        System.out.println("Is GroupMessageEvent: " + (event instanceof GroupMessageEvent));

        // 设置 rawData - 使用 GSON 直接转换为 Map
        @SuppressWarnings("unchecked")
        Map<String, Object> rawData = gson.fromJson(jsonStr, Map.class);
        event.setRawData(rawData);

        System.out.println("rawData message_type: " + rawData.get("message_type"));
        System.out.println("event.get(message_type): " + event.get("message_type"));

        // 验证关键点
        assertTrue(event instanceof GroupMessageEvent, "应该是 GroupMessageEvent 类型");
        assertEquals("group", event.get("message_type"), "message_type 应该是 group");
    }

    @Test
    @DisplayName("测试 GroupMessageListener.isGroupMessage 逻辑")
    void testIsGroupMessageLogic() {
        String json = "{\"self_id\":2784152733,\"user_id\":1751649231,\"message_type\":\"group\"," +
                "\"group_id\":345693826,\"raw_message\":\"test\",\"post_type\":\"message\"}";

        Event event = gson.fromJson(json, GroupMessageEvent.class);

        Map<String, Object> rawData = new HashMap<>();
        rawData.put("message_type", "group");
        rawData.put("post_type", "message");
        event.setRawData(rawData);

        // 模拟 GroupMessageListener.isGroupMessage() 的逻辑
        boolean isGroupMessage = "group".equals(event.get("message_type"));

        assertTrue(isGroupMessage, "应该被识别为群消息");
    }

    @Test
    @DisplayName("测试完整消息流 - 从解析到过滤")
    void testFullMessageFlow() {
        // 1. 模拟 Controller 返回的 JSON
        String jsonStr = "{\"self_id\":2784152733,\"user_id\":1751649231,\"message_type\":\"group\"," +
                "\"group_id\":345693826,\"raw_message\":\"!hello\",\"post_type\":\"message\"}";

        // 2. 模拟 parseEvent
        com.google.gson.JsonObject jsonObj = com.google.gson.JsonParser.parseString(jsonStr).getAsJsonObject();
        String postType = jsonObj.has("post_type") ? jsonObj.get("post_type").getAsString() : "";
        String messageType = jsonObj.has("message_type") ? jsonObj.get("message_type").getAsString() : "";

        Class<? extends Event> eventClass = "message".equals(postType) && "group".equals(messageType)
                ? GroupMessageEvent.class : Event.class;

        Event event = gson.fromJson(jsonStr, eventClass);

        // 设置 rawData - 使用 GSON 直接转换为 Map
        @SuppressWarnings("unchecked")
        Map<String, Object> rawData = gson.fromJson(jsonStr, Map.class);
        event.setRawData(rawData);

        System.out.println("Step 1: Parsed event class = " + event.getClass().getName());

        // 3. 模拟 isGroupMessage 过滤
        boolean isGroupMessage = "group".equals(event.get("message_type"));
        System.out.println("Step 2: isGroupMessage = " + isGroupMessage);

        assertTrue(isGroupMessage, "应该通过过滤");

        // 4. 验证消息内容
        GroupMessageEvent groupEvent = (GroupMessageEvent) event;
        System.out.println("Step 3: groupId = " + groupEvent.getGroupId() + ", rawMessage = " + groupEvent.getRawMessage());

        assertEquals("345693826", groupEvent.getGroupId(), "groupId 应该正确");
        assertEquals("!hello", groupEvent.getRawMessage(), "rawMessage 应该正确");
    }
}
