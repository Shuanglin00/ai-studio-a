package com.shuanglin.executor;

import com.shuanglin.framework.annotation.GroupMessageHandler;
import com.shuanglin.framework.bus.event.GroupMessageEvent;
import com.shuanglin.framework.config.BotProperties;
import com.shuanglin.framework.enums.RoleType;
import com.shuanglin.framework.onebot.builder.GroupMessageBuilder;
import com.shuanglin.framework.onebot.segment.ImageSegment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Random;

/**
 * 猪图片消息处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PigGroupMessageExecutor {

    private final BotProperties botProperties;

    /**
     * 处理群消息，发送随机猪图片
     *
     * 消息格式示例:
     * {"self_id":2784152733,"user_id":1751649231,"message_type":"group",
     *  "group_id":345693826,"sender":{"user_id":1751649231,"role":"owner"},
     *  "raw_message":"渚","sub_type":"normal"}
     */
    @GroupMessageHandler(triggerPrefix = "渚", description = "发送随机猪猪图片",role = RoleType.User)
    public void pigGroupMessage(GroupMessageEvent group) {
        String imagePath = botProperties.getPig().getImagePath();
        String base64Image = getRandomImageAsBase64(imagePath);

        if (base64Image == null) {
            log.error("Failed to load pig image from: {}", imagePath);
            GroupMessageBuilder.forGroup(group.getGroupId())
                    .text("❌ 无法加载图片，请稍后再试")
                    .send();
            return;
        }

        try {
            GroupMessageBuilder.forGroup(group.getGroupId())
                    .reply(group.getMessageId())
                    .image(base64Image)
                    .send();
            log.info("Sent pig image to group: {}", group.getGroupId());
        } catch (Exception e) {
            log.error("Failed to send pig image", e);
        }
    }

    /**
     * 从指定目录随机获取一张图片并转为 Base64 格式
     *
     * @param directoryPath 图片目录路径
     * @return Base64 格式的图片数据，失败返回 null
     */
    private String getRandomImageAsBase64(String directoryPath) {
        File directory = resolveDirectory(directoryPath);
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            log.warn("Image directory not found or invalid: {}", directoryPath);
            return null;
        }

        File[] imageFiles = directory.listFiles(createImageFilter());
        if (imageFiles == null || imageFiles.length == 0) {
            log.warn("No image files found in directory: {}", directoryPath);
            return null;
        }

        File randomImageFile = imageFiles[new Random().nextInt(imageFiles.length)];
        log.debug("Loading image: {}", randomImageFile.getAbsolutePath());

        return loadImageAsBase64(randomImageFile);
    }

    /**
     * 解析目录路径，支持占位符
     */
    private File resolveDirectory(String path) {
        // 解析 ${user.dir} 占位符
        String resolvedPath = path.replace("${user.dir}", System.getProperty("user.dir"));
        return new File(resolvedPath);
    }

    /**
     * 创建图片文件过滤器
     */
    private FilenameFilter createImageFilter() {
        return (dir, name) -> {
            String lowerName = name.toLowerCase();
            return lowerName.endsWith(".jpg") || lowerName.endsWith(".png")
                    || lowerName.endsWith(".gif") || lowerName.endsWith(".jpeg");
        };
    }

    /**
     * 加载图片文件并转为 Base64 格式
     */
    private String loadImageAsBase64(File imageFile) {
        try {
            byte[] fileContent = Files.readAllBytes(imageFile.toPath());
            return "base64://" + Base64.getEncoder().encodeToString(fileContent);
        } catch (IOException e) {
            log.error("Failed to read image file: {}", imageFile.getAbsolutePath(), e);
            return null;
        }
    }
}
