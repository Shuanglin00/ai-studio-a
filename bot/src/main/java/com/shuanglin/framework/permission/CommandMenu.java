package com.shuanglin.framework.permission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 指令菜单
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommandMenu {

    /**
     * 全局指令列表
     */
    private List<CommandItem> globalCommands;

    /**
     * 群聊指令列表
     */
    private List<CommandItem> groupCommands;

    /**
     * 私聊指令列表
     */
    private List<CommandItem> privateCommands;
}
