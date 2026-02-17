package com.shuanglin.mcp.tool;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.CyclicBufferAppender;
import jakarta.annotation.PostConstruct;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * MCP工具：捕获和获取应用程序日志
 * 用于提供给Claude Code进行自我检查和修复验证
 */
@Component
public class LogCaptureTool {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(LogCaptureTool.class);
    private static final String APPENDER_NAME = "MCP_LOG_CAPTURE";
    private static final int BUFFER_SIZE = 1000;

    private CyclicBufferAppender<ILoggingEvent> bufferAppender;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
            .withZone(ZoneId.systemDefault());

    @PostConstruct
    public void init() {
        setupLogCapture();
    }

    /**
     * 设置日志捕获
     */
    private void setupLogCapture() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);

        // 移除已有的appender防止重复
        if (bufferAppender != null) {
            rootLogger.detachAppender(bufferAppender);
        }

        bufferAppender = new CyclicBufferAppender<>();
        bufferAppender.setName(APPENDER_NAME);
        bufferAppender.setMaxSize(BUFFER_SIZE);
        bufferAppender.setContext(loggerContext);
        bufferAppender.start();

        rootLogger.addAppender(bufferAppender);
        logger.info("Log capture initialized with buffer size: {}", BUFFER_SIZE);
    }

    /**
     * 获取最近的日志输出
     *
     * @param lines 要获取的日志行数（默认100，最大500）
     * @param levelFilter 日志级别过滤（可选: ERROR, WARN, INFO, DEBUG, TRACE）
     * @return 格式化的日志内容
     */
    @Tool(name = "getRecentLogs", description = "获取应用程序最近的日志输出，用于自我检查和修复验证。支持按日志级别过滤。")
    public String getRecentLogs(
            @ToolParam(description = "要获取的日志行数，默认100，最大500") int lines,
            @ToolParam(description = "日志级别过滤（可选: ERROR, WARN, INFO, DEBUG, TRACE），不填则返回所有级别") String levelFilter) {

        if (lines <= 0) {
            lines = 100;
        }
        lines = Math.min(lines, 500);

        if (bufferAppender == null) {
            return "Error: Log capture not initialized";
        }

        List<String> logEntries = new ArrayList<>();
        int count = bufferAppender.getLength();

        // 计算起始索引
        int startIndex = Math.max(0, count - lines);

        for (int i = startIndex; i < count; i++) {
            ILoggingEvent event = bufferAppender.get(i);

            // 应用级别过滤
            if (levelFilter != null && !levelFilter.isBlank()) {
                if (!event.getLevel().toString().equalsIgnoreCase(levelFilter.trim())) {
                    continue;
                }
            }

            String formatted = formatLogEntry(event);
            logEntries.add(formatted);
        }

        if (logEntries.isEmpty()) {
            return "No log entries found matching the criteria.";
        }

        return String.join("\n", logEntries);
    }

    /**
     * 获取最近的错误日志
     *
     * @param lines 要获取的错误日志行数（默认50，最大200）
     * @return 格式化的错误日志内容
     */
    @Tool(name = "getRecentErrors", description = "获取最近的ERROR级别日志，用于快速定位问题。")
    public String getRecentErrors(
            @ToolParam(description = "要获取的错误日志行数，默认50，最大200") int lines) {
        return getRecentLogs(lines, "ERROR");
    }

    /**
     * 搜索日志内容
     *
     * @param keyword 搜索关键词
     * @param maxResults 最大返回结果数（默认20，最大100）
     * @return 匹配的日志内容
     */
    @Tool(name = "searchLogs", description = "在日志中搜索包含指定关键词的日志行。")
    public String searchLogs(
            @ToolParam(description = "搜索关键词") String keyword,
            @ToolParam(description = "最大返回结果数，默认20，最大100") int maxResults) {

        if (keyword == null || keyword.isBlank()) {
            return "Error: Search keyword cannot be empty";
        }

        if (maxResults <= 0) {
            maxResults = 20;
        }
        maxResults = Math.min(maxResults, 100);

        if (bufferAppender == null) {
            return "Error: Log capture not initialized";
        }

        List<String> matchingEntries = new ArrayList<>();
        int count = bufferAppender.getLength();
        String searchLower = keyword.toLowerCase();

        // 从最新的日志开始搜索
        for (int i = count - 1; i >= 0 && matchingEntries.size() < maxResults; i--) {
            ILoggingEvent event = bufferAppender.get(i);
            String message = event.getFormattedMessage();

            // 检查消息内容
            if (message != null && message.toLowerCase().contains(searchLower)) {
                matchingEntries.add(0, formatLogEntry(event)); // 插入到开头保持时间顺序
                continue;
            }

            // 检查异常堆栈
            if (event.getThrowableProxy() != null) {
                String stackTrace = getStackTraceString(event);
                if (stackTrace.toLowerCase().contains(searchLower)) {
                    matchingEntries.add(0, formatLogEntry(event));
                }
            }
        }

        if (matchingEntries.isEmpty()) {
            return "No log entries found containing: " + keyword;
        }

        return String.join("\n", matchingEntries);
    }

    /**
     * 获取日志统计信息
     *
     * @return 日志统计摘要
     */
    @Tool(name = "getLogStats", description = "获取当前日志缓冲区的统计信息。")
    public String getLogStats() {
        if (bufferAppender == null) {
            return "Error: Log capture not initialized";
        }

        int totalCount = bufferAppender.getLength();
        int errorCount = 0;
        int warnCount = 0;
        int infoCount = 0;
        int debugCount = 0;

        for (int i = 0; i < totalCount; i++) {
            ILoggingEvent event = bufferAppender.get(i);
            switch (event.getLevel().toString()) {
                case "ERROR" -> errorCount++;
                case "WARN" -> warnCount++;
                case "INFO" -> infoCount++;
                case "DEBUG" -> debugCount++;
            }
        }

        return String.format("""
                Log Statistics:
                ==============
                Total entries in buffer: %d
                ERROR: %d
                WARN: %d
                INFO: %d
                DEBUG: %d
                Buffer capacity: %d
                """,
                totalCount, errorCount, warnCount, infoCount, debugCount, BUFFER_SIZE);
    }

    /**
     * 格式化日志条目
     */
    private String formatLogEntry(ILoggingEvent event) {
        StringBuilder sb = new StringBuilder();

        // 时间戳
        sb.append(dateFormatter.format(Instant.ofEpochMilli(event.getTimeStamp())))
                .append(" ");

        // 日志级别
        sb.append("[").append(padRight(event.getLevel().toString(), 5)).append("] ");

        // Logger名称（简化为类名）
        String loggerName = event.getLoggerName();
        if (loggerName.contains(".")) {
            loggerName = loggerName.substring(loggerName.lastIndexOf(".") + 1);
        }
        sb.append(padRight(loggerName, 30)).append(" - ");

        // 消息内容
        sb.append(event.getFormattedMessage());

        // 异常信息
        if (event.getThrowableProxy() != null) {
            sb.append("\n").append(getStackTraceString(event));
        }

        return sb.toString();
    }

    /**
     * 获取异常堆栈字符串
     */
    private String getStackTraceString(ILoggingEvent event) {
        StringBuilder sb = new StringBuilder();
        if (event.getThrowableProxy() != null) {
            var throwable = event.getThrowableProxy();
            sb.append(throwable.getClassName()).append(": ").append(throwable.getMessage());

            for (int i = 0; i < throwable.getStackTraceElementProxyArray().length; i++) {
                var element = throwable.getStackTraceElementProxyArray()[i];
                sb.append("\n    at ").append(element.getSTEAsString());
            }
        }
        return sb.toString();
    }

    private String padRight(String s, int n) {
        if (s.length() >= n) {
            return s.substring(0, n);
        }
        return String.format("%-" + n + "s", s);
    }
}
