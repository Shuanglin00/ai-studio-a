package com.shuanglin.dao.bot;

import com.shuanglin.dao.base.BaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Document(collection = "bot_commands")
public class Command extends BaseEntity {

    private String commandName;
    private String triggerPrefix;
    private String role;
    private String description;
}
