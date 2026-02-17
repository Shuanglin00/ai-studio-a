package com.shuanglin.mcp.tool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.shuanglin.dao.classification.CollectedMessage;
import com.shuanglin.dao.classification.MessageCollectionBatch;
import com.shuanglin.dao.classification.enums.BatchStatus;
import com.shuanglin.dao.classification.enums.MessageCategory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Mock数据生成工具
 * 用于生成模拟消息批次用于测试
 */
@Component
public class MockDataGeneratorTool {

    private final ObjectMapper objectMapper;

    // 不同分类的Mock消息模板
    private static final String[] MEME_MESSAGES = {
            "哈哈哈哈哈哈哈",
            "笑死我了",
            "这图太搞了",
            "什么弔图",
            "草",
            "太抽象了",
            "蚌埠住了",
            "什么b动静",
            "笑得我肚子疼",
            "什么鬼东西",
            "绝了这图",
            "哈哈哈嗝"
    };

    private static final String[] HELL_JOKE_MESSAGES = {
            "这也太地狱了",
            "不敢笑",
            "的地狱笑话",
            "要下地狱了",
            "功德-1",
            "敲个电子木鱼",
            "绷不住了",
            "什么阴间笑话",
            "扣1佛祖原谅你"
    };

    private static final String[] REGIONAL_BLACK_MESSAGES = {
            "你们那是不是",
            "听说你们那边",
            "经典XX人",
            "不愧是XX的",
            "你们那特色",
            "懂的都懂",
            "地域特色"
    };

    private static final String[] NORMAL_MESSAGES = {
            "今天天气不错",
            "吃饭了吗",
            "在干嘛",
            "晚上好",
            "早点休息",
            "好的",
            "收到",
            "谢谢",
            "没问题",
            "可以"
    };

    private static final String[] SPAM_MESSAGES = {
            "？？？",
            "！！！",
            "111",
            "666",
            "???",
            "!!!",
            "哈哈哈哈",
            "wow",
            "nb"
    };

    private static final String[] AD_MESSAGES = {
            "加群",
            "了解一下",
            "有兴趣",
            "特价",
            "优惠",
            "包邮",
            "限时",
            "抢购"
    };

    private static final String[] NICKNAMES = {
            "小明", "小红", "张三", "李四", "王五", "赵六", "测试用户", "匿名用户",
            "吃瓜群众", "路过打酱油", "键盘侠", "潜水员", "大佬", "萌新"
    };

    public MockDataGeneratorTool() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * 生成模拟消息批次
     *
     * @param groupId     群号
     * @param messageCount 消息数量
     * @param category    分类类型
     * @return 批次JSON字符串
     */
    @Tool(name = "generateMockMessageBatch",
            description = "生成模拟消息批次用于测试，可根据分类类型生成对应风格的消息内容")
    public String generateMockMessageBatch(
            @ToolParam(description = "群号") String groupId,
            @ToolParam(description = "消息数量（1-50）") int messageCount,
            @ToolParam(description = "分类类型：MEME/HELL_JOKE/REGIONAL_BLACK/NORMAL/SPAM/AD") String category) {

        // 参数验证
        if (groupId == null || groupId.trim().isEmpty()) {
            throw new IllegalArgumentException("Group ID cannot be empty");
        }
        if (messageCount < 1 || messageCount > 50) {
            throw new IllegalArgumentException("Message count must be between 1 and 50");
        }

        MessageCategory messageCategory;
        try {
            messageCategory = MessageCategory.valueOf(category);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid category: " + category);
        }

        // 根据分类生成消息
        List<CollectedMessage> messages = generateMessagesByCategory(messageCategory, messageCount);

        // 构建批次
        MessageCollectionBatch batch = MessageCollectionBatch.builder()
                .id(UUID.randomUUID().toString())
                .groupId(groupId)
                .triggerMessageId(System.currentTimeMillis())
                .triggerMessageType("IMAGE")
                .triggerContent("Mock trigger message")
                .messages(messages)
                .messageCount(messages.size())
                .collectStartTime(LocalDateTime.now().minusMinutes(5))
                .collectEndTime(LocalDateTime.now())
                .status(BatchStatus.COMPLETED)
                .build();

        try {
            return objectMapper.writeValueAsString(batch);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize batch to JSON", e);
        }
    }

    /**
     * 根据分类生成对应风格的消息
     */
    private List<CollectedMessage> generateMessagesByCategory(MessageCategory category, int count) {
        List<CollectedMessage> messages = new ArrayList<>();
        String[] templates = getTemplatesForCategory(category);

        long baseTimestamp = System.currentTimeMillis() - (count * 60000L); // 每条消息间隔1分钟

        for (int i = 0; i < count; i++) {
            String content = templates[i % templates.length];
            if (i >= templates.length) {
                // 超出模板数量时，随机组合
                content = templates[(int) (Math.random() * templates.length)];
            }

            CollectedMessage message = CollectedMessage.builder()
                    .messageId(baseTimestamp + i)
                    .userId(10000L + (long) (Math.random() * 90000))
                    .nickname(NICKNAMES[(int) (Math.random() * NICKNAMES.length)])
                    .content(content)
                    .timestamp(baseTimestamp + (i * 60000L))
                    .isReply(false)
                    .replyToMessageId(null)
                    .build();

            messages.add(message);
        }

        return messages;
    }

    /**
     * 获取指定分类的消息模板
     */
    private String[] getTemplatesForCategory(MessageCategory category) {
        return switch (category) {
            case MEME -> MEME_MESSAGES;
            case HELL_JOKE -> HELL_JOKE_MESSAGES;
            case REGIONAL_BLACK -> REGIONAL_BLACK_MESSAGES;
            case SPAM -> SPAM_MESSAGES;
            case AD -> AD_MESSAGES;
            case NORMAL, POLITICAL, NSFW, OTHER -> NORMAL_MESSAGES;
        };
    }
}
