package com.shuanglin.framework.milky.model.response;

import com.shuanglin.framework.milky.enums.MessageSubType;
import com.shuanglin.framework.milky.enums.MessageType;
import com.shuanglin.framework.milky.enums.Sex;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 消息内容
 */
@Data
public class MessageContent implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 消息ID
     */
    private Long messageId;

    /**
     * 消息序列号
     */
    private Long messageSeq;

    /**
     * 消息类型
     */
    private MessageType type;

    /**
     * 消息子类型
     */
    private MessageSubType subType;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 发送者用户ID
     */
    private Long userId;

    /**
     * 发送者昵称
     */
    private String nickname;

    /**
     * 发送者性别
     */
    private Sex sex;

    /**
     * 发送者年龄
     */
    private Integer age;

    /**
     * 发送者群ID（如果是群消息）
     */
    private Long groupId;

    /**
     * 消息段列表
     */
    private List<Map<String, Object>> message;

    /**
     * 原始消息
     */
    private String rawMessage;

    /**
     * 发送时间
     */
    private Long time;

    /**
     * 消息来源
     */
    private String messageScene;
}
