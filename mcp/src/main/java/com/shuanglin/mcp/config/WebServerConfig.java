package com.shuanglin.mcp.config;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.shuanglin.mcp.tool.*;

/**
 * MCP服务器配置类
 * 注册所有MCP工具
 */
@Configuration
public class WebServerConfig {

    /**
     * 注册日志捕获工具
     */
    @Bean
    public ToolCallbackProvider logCaptureToolProvider(LogCaptureTool logCaptureTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(logCaptureTool)
                .build();
    }

    /**
     * 注册Web搜索工具
     */
    @Bean
    public ToolCallbackProvider webSearchToolProvider(WebSearchTool webSearchTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(webSearchTool)
                .build();
    }

    /**
     * 注册Mock数据生成工具
     */
    @Bean
    public ToolCallbackProvider mockDataGeneratorToolProvider(MockDataGeneratorTool mockDataGeneratorTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(mockDataGeneratorTool)
                .build();
    }

    /**
     * 注册消息存储工具
     */
    @Bean
    public ToolCallbackProvider messageStorageToolProvider(MessageStorageTool messageStorageTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(messageStorageTool)
                .build();
    }

    /**
     * 注册分类管理工具
     */
    @Bean
    public ToolCallbackProvider categoryManagementToolProvider(CategoryManagementTool categoryManagementTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(categoryManagementTool)
                .build();
    }

    /**
     * 注册消息分类工具
     */
    @Bean
    public ToolCallbackProvider messageClassificationToolProvider(MessageClassificationTool messageClassificationTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(messageClassificationTool)
                .build();
    }
}
