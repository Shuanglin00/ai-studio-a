package com.shuanglin.dao.bot;

import lombok.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户会话信息
 * 存储于Redis，维护用户级别的会话状态
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class UserSession implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户QQ号
     */
    private String userId;

    /**
     * 群号
     */
    private String groupId;

    /**
     * 当前选择的模型
     */
    private String currentModel;

    /**
     * 会话附加数据
     */
    @Builder.Default
    private Map<String, Object> sessionData = new HashMap<>();
}
