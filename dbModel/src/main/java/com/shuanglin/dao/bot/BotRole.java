package com.shuanglin.dao.bot;

import com.shuanglin.dao.base.BaseEntity;
import com.shuanglin.dao.model.Model;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * 机器人角色实体
 * 代表机器人的一类功能特征，包含一组相关指令
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Document(collection = "bot_roles")
public class BotRole extends BaseEntity {

    /**
     * 角色唯一标识（雪花ID）
     */
    @Id
    private String id;

    /**
     * 角色名称（唯一索引）
     */
    @Indexed(unique = true)
    private String roleName;

    /**
     * 角色描述
     */
    private String description;

    /**
     * 指令名称列表
     */
    private List<String> commandNames;

    /**
     * AI模型配置（可选）
     */
    private Model modelConfig;

    /**
     * 全局启用状态
     */
    @Indexed
    private Boolean isActive;
}
