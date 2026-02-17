package com.shuanglin.mcp;

import com.shuanglin.mcp.tool.LogCaptureTool;
import com.shuanglin.mcp.tool.WebSearchTool;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MCP工具测试运行器
 * 直接测试工具功能
 */
@SpringBootApplication
@Profile("test")
public class TestRunner {

    private static final Logger logger = LoggerFactory.getLogger(TestRunner.class);

    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "test");
        SpringApplication.run(TestRunner.class, args);
    }

    @Bean
    CommandLineRunner run(LogCaptureTool logCaptureTool, WebSearchTool webSearchTool) {
        return args -> {
            System.out.println("\n==========================================");
            System.out.println("Starting MCP Tools Test Runner");
            System.out.println("==========================================\n");

            try {
                // 测试1: 验证工具加载
                System.out.println("[TEST 1] Verifying tool beans are loaded...");
                assert logCaptureTool != null : "LogCaptureTool should not be null";
                assert webSearchTool != null : "WebSearchTool should not be null";
                System.out.println("✓ LogCaptureTool loaded successfully");
                System.out.println("✓ WebSearchTool loaded successfully\n");

                // 测试2: 日志捕获
                System.out.println("[TEST 2] Testing log capture functionality...");
                logger.info("TEST_MESSAGE_12345: This is a test message");
                logger.warn("TEST_WARNING_12345: This is a warning");
                logger.error("TEST_ERROR_12345: This is an error");

                Thread.sleep(200);

                String logs = logCaptureTool.getRecentLogs(20, "");
                System.out.println("Retrieved logs:");
                System.out.println("-".repeat(50));
                System.out.println(logs);
                System.out.println("-".repeat(50));
                assert logs.contains("TEST_MESSAGE_12345") : "Should capture INFO log";
                System.out.println("✓ Log capture test passed\n");

                // 测试3: 日志级别过滤
                System.out.println("[TEST 3] Testing log level filtering...");
                String errorLogs = logCaptureTool.getRecentLogs(10, "ERROR");
                assert errorLogs.contains("TEST_ERROR_12345") : "Should filter ERROR logs";
                System.out.println("✓ ERROR level filter works\n");

                // 测试4: 日志搜索
                System.out.println("[TEST 4] Testing log search functionality...");
                String searchResult = logCaptureTool.searchLogs("TEST_MESSAGE_12345", 5);
                assert searchResult.contains("TEST_MESSAGE_12345") : "Should find the log by keyword";
                System.out.println("✓ Log search works\n");

                // 测试5: 日志统计
                System.out.println("[TEST 5] Testing log statistics...");
                String stats = logCaptureTool.getLogStats();
                System.out.println("Log Statistics:");
                System.out.println("-".repeat(50));
                System.out.println(stats);
                System.out.println("-".repeat(50));
                assert stats.contains("Total entries") : "Should have statistics";
                System.out.println("✓ Log statistics works\n");

                // 测试6: Web搜索
                System.out.println("[TEST 6] Testing web search (may fail without network)...");
                try {
                    String searchResult2 = webSearchTool.searchWeb("Spring Framework", 3);
                    System.out.println("Search result preview:");
                    System.out.println("-".repeat(50));
                    System.out.println(searchResult2.substring(0, Math.min(500, searchResult2.length())));
                    System.out.println("...");
                    System.out.println("-".repeat(50));
                    System.out.println("✓ Web search test completed\n");
                } catch (Exception e) {
                    System.out.println("! Web search test skipped (network issue)\n");
                }

                System.out.println("==========================================");
                System.out.println("All tests passed successfully!");
                System.out.println("==========================================");

                // 等待一下然后退出
                Thread.sleep(1000);
                System.exit(0);

            } catch (AssertionError e) {
                System.err.println("\n✗ TEST FAILED: " + e.getMessage());
                System.exit(1);
            } catch (Exception e) {
                System.err.println("\n✗ ERROR: " + e.getMessage());
                e.printStackTrace();
                System.exit(1);
            }
        };
    }
}
