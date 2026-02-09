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
 * 群聊配置实体
 * 存储每个群聊的独立配置信息，控制该群可用的功能
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Document(collection = "group_configurations")
public class GroupConfiguration extends BaseEntity {

    /**
     * 配置ID（雪花ID）
     */
    @Id
    private String id;

    /**
     * 群号（唯一索引）
     */
    @Indexed(unique = true)
    private String groupId;

    /**
     * 已启用的角色名称列表（多键索引）
     */
    @Indexed
    private List<String> enabledRoles;

    /**
     * 管理员QQ号列表（机器人管理员）
     */
    private List<String> admins;

    /**
     * 群聊级别AI模型配置
     */
    private Model modelConfig;
}
