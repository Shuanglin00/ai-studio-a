package com.shuanglin.executor;

import com.shuanglin.framework.annotation.BotCommand;
import com.shuanglin.framework.annotation.GroupMessageHandler;
import com.shuanglin.framework.bus.event.GroupMessageEvent;
import com.shuanglin.framework.milky.builder.GroupMessageBuilder;
import com.shuanglin.service.MessageCollectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 手动标注执行器
 * 处理 #标记 指令
 */
@Slf4j
@Component
@RequiredArgsConstructor
@BotCommand(role = "内容标注", description = "手动标记消息分类")
public class TaggingExecutor {

    private final MessageCollectionService messageCollectionService;

    // 有效的分类列表
    private static final List<String> VALID_CATEGORIES = Arrays.asList(
            "弔图", "meme",
            "地狱笑话", "hell-joke",
            "地域黑", "regional-black",
            "正常", "normal",
            "广告", "ad",
            "刷屏", "spam"
    );

    /**
     * 处理标记指令
     * 格式: #标记 [分类名]
     * 需要引用一条消息
     */
    @GroupMessageHandler(startWith = "#标记", description = "标记引用消息的分类")
    public void handleTagCommand(GroupMessageEvent event) {
        log.info("Handling tag command: {}", event.getMessage());

        String message = event.getMessage();

        // 检查是否有引用消息
        if (event.getReplyMessageId() == null) {
            sendErrorMessage(event, "请先引用要标记的消息");
            return;
        }

        // 解析分类
        String category = parseCategory(message);
        if (category == null) {
            sendErrorMessage(event, "请指定有效的分类：弔图、地狱笑话、地域黑、正常、广告、刷屏");
            return;
        }

        // 执行标记
        try {
            messageCollectionService.updateClassification(
                    String.valueOf(event.getReplyMessageId()),
                    category,
                    event.getUserId()
            );

            sendSuccessMessage(event, category);

        } catch (Exception e) {
            log.error("Failed to tag message", e);
            sendErrorMessage(event, "标记失败：" + e.getMessage());
        }
    }

    /**
     * 查看分类帮助
     */
    @GroupMessageHandler(startWith = "#标记帮助", description = "查看标记功能帮助")
    public void showTagHelp(GroupMessageEvent event) {
        String helpText = """
                【消息标记功能】

                使用方法：
                1. 引用要标记的消息
                2. 发送：#标记 [分类名]

                可用分类：
                • 弔图/meme - 搞笑图片、表情包
                • 地狱笑话/hell-joke - 黑色幽默
                • 地域黑/regional-black - 地域歧视言论
                • 正常/normal - 普通聊天内容
                • 广告/ad - 商业广告
                • 刷屏/spam - 无意义重复内容

                示例：
                [引用消息] #标记 弔图
                """;

        GroupMessageBuilder.forGroup(event.getGroupId())
                .text(helpText)
                .send();
    }

    /**
     * 解析分类
     */
    private String parseCategory(String message) {
        // 移除指令前缀
        String content = message.replaceFirst("#标记\\s*", "").trim();

        if (content.isEmpty()) {
            return null;
        }

        // 映射到标准分类编码
        return switch (content.toLowerCase()) {
            case "弔图", "meme", "搞笑" -> "meme";
            case "地狱笑话", "hell-joke", "地狱" -> "hell-joke";
            case "地域黑", "regional-black", "地域" -> "regional-black";
            case "正常", "normal" -> "normal";
            case "广告", "ad" -> "ad";
            case "刷屏", "spam" -> "spam";
            default -> null;
        };
    }

    /**
     * 发送成功消息
     */
    private void sendSuccessMessage(GroupMessageEvent event, String category) {
        GroupMessageBuilder.forGroup(event.getGroupId())
                .at(event.getUserId())
                .text("已标记为【" + category + "】")
                .send();
    }

    /**
     * 发送错误消息
     */
    private void sendErrorMessage(GroupMessageEvent event, String error) {
        GroupMessageBuilder.forGroup(event.getGroupId())
                .at(event.getUserId())
                .text("标记失败：" + error)
                .send();
    }
}
