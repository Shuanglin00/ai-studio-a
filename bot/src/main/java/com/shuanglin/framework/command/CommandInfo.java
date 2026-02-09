package com.shuanglin.framework.command;

import com.shuanglin.framework.enums.RoleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;

/**
 * 指令信息模型
 * 扩展MethodInfo，增加指令元数据
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommandInfo {

    /**
     * 指令名称（自动提取自方法名或注解）
     */
    private String commandName;

    /**
     * 触发前缀（从@GroupMessageHandler.startWith获取）
     */
    private String triggerPrefix;

    /**
     * 所属角色名称
     */
    private RoleType role;

    /**
     * 是否需要管理员权限
     */
    @Builder.Default
    private Boolean requireAdmin = false;

    /**
     * 指令描述
     */
    private String description;

    /**
     * Spring Bean实例
     */
    private Object bean;

    /**
     * 处理方法
     */
    private Method method;

    /**
     * SpEL条件表达式
     */
    private String condition;
}
