package com.shuanglin.mcp.tool;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * MCP工具：Web搜索功能
 * 支持多种搜索引擎API
 */
@Component
public class WebSearchTool {

    private static final Logger logger = LoggerFactory.getLogger(WebSearchTool.class);

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public WebSearchTool() {
        this.webClient = WebClient.builder()
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.0")
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 执行Web搜索
     *
     * @param query 搜索查询
     * @param numResults 返回结果数量（默认5，最大10）
     * @return 搜索结果
     */
    @Tool(name = "searchWeb", description = "搜索互联网获取最新信息。支持一般搜索查询，返回标题、摘要和URL。")
    public String searchWeb(
            @ToolParam(description = "搜索查询关键词") String query,
            @ToolParam(description = "返回结果数量，默认5，最大10") int numResults) {

        if (query == null || query.isBlank()) {
            return "Error: Search query cannot be empty";
        }

        if (numResults <= 0) {
            numResults = 5;
        }
        numResults = Math.min(numResults, 10);

        try {
            // 使用DuckDuckGo进行搜索（无需API key）
            return searchWithDuckDuckGo(query, numResults);
        } catch (Exception e) {
            logger.error("Web search failed for query: {}", query, e);
            return "Search failed: " + e.getMessage();
        }
    }

    /**
     * 使用DuckDuckGo搜索（无需API Key）
     */
    private String searchWithDuckDuckGo(String query, int numResults) {
        try {
            // DuckDuckGo HTML搜索并解析
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = "https://html.duckduckgo.com/html/?q=" + encodedQuery;

            String html = webClient.get()
                    .uri(url)
                    .header(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            if (html == null) {
                return "No results found";
            }

            return parseDuckDuckGoResults(html, numResults);

        } catch (Exception e) {
            logger.error("DuckDuckGo search failed", e);
            // 降级到模拟搜索或备用方案
            return performFallbackSearch(query, numResults);
        }
    }

    /**
     * 解析DuckDuckGo HTML结果
     */
    private String parseDuckDuckGoResults(String html, int numResults) {
        List<SearchResult> results = new ArrayList<>();

        // 简单的HTML解析 - 提取搜索结果
        // DuckDuckGo结果格式: <div class="result"...
        String resultDiv = "<div class=\"result\"";
        String titleStart = "<a rel=\"nofollow\" class=\"result__a\"";
        String snippetStart = "<a class=\"result__snippet\"";

        int startIndex = 0;
        int foundCount = 0;

        while (foundCount < numResults) {
            int resultStart = html.indexOf(resultDiv, startIndex);
            if (resultStart == -1) break;

            int resultEnd = html.indexOf(resultDiv, resultStart + resultDiv.length());
            if (resultEnd == -1) resultEnd = html.length();

            String resultBlock = html.substring(resultStart, resultEnd);

            // 提取标题和链接
            String title = extractContent(resultBlock, ">", "</a>");
            String url = extractAttribute(resultBlock, "href=\"", "\"");

            // 提取摘要
            int snippetIdx = resultBlock.indexOf(snippetStart);
            String snippet = "";
            if (snippetIdx != -1) {
                snippet = extractContent(resultBlock.substring(snippetIdx), ">", "</a>");
            }

            if (!title.isEmpty() && !url.isEmpty()) {
                SearchResult result = new SearchResult();
                result.setTitle(cleanHtml(title));
                result.setUrl(url);
                result.setSnippet(cleanHtml(snippet));
                results.add(result);
                foundCount++;
            }

            startIndex = resultEnd;
        }

        if (results.isEmpty()) {
            return "No results found for the query.";
        }

        return formatResults(results);
    }

    private String extractContent(String html, String startDelimiter, String endDelimiter) {
        int start = html.indexOf(startDelimiter);
        if (start == -1) return "";
        start += startDelimiter.length();
        int end = html.indexOf(endDelimiter, start);
        if (end == -1) return "";
        return html.substring(start, end);
    }

    private String extractAttribute(String html, String attrStart, String attrEnd) {
        int start = html.indexOf(attrStart);
        if (start == -1) return "";
        start += attrStart.length();
        int end = html.indexOf(attrEnd, start);
        if (end == -1) return "";
        return html.substring(start, end);
    }

    private String cleanHtml(String html) {
        if (html == null) return "";
        return html.replaceAll("<[^\u003e]*>", "")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .trim();
    }

    /**
     * 备用搜索方案
     */
    private String performFallbackSearch(String query, int numResults) {
        // 返回一个友好的错误消息，说明搜索服务暂时不可用
        return String.format("""
                Search Service Temporarily Unavailable
                =======================================
                Query: %s

                The web search service is currently experiencing issues.
                Possible reasons:
                1. Network connectivity problems
                2. Rate limiting from search provider
                3. Service temporarily down

                Please try again later or use local knowledge.
                """, query);
    }

    /**
     * 格式化搜索结果
     */
    private String formatResults(List<SearchResult> results) {
        StringBuilder sb = new StringBuilder();
        sb.append("Web Search Results\n");
        sb.append("==================\n\n");

        for (int i = 0; i < results.size(); i++) {
            SearchResult result = results.get(i);
            sb.append(String.format("%d. %s\n", i + 1, result.getTitle()));
            sb.append(String.format("   URL: %s\n", result.getUrl()));
            if (result.getSnippet() != null && !result.getSnippet().isEmpty()) {
                sb.append(String.format("   %s\n", result.getSnippet()));
            }
            sb.append("\n");
        }

        return sb.toString().trim();
    }

    /**
     * 使用SearXNG搜索（如果配置了私有实例）
     */
    @Tool(name = "searchWithSearx", description = "使用SearXNG搜索引擎进行搜索（需要配置私有实例）。")
    public String searchWithSearx(
            @ToolParam(description = "搜索查询") String query,
            @ToolParam(description = "SearXNG实例URL") String searxUrl,
            @ToolParam(description = "返回结果数量") int numResults) {

        if (query == null || query.isBlank()) {
            return "Error: Search query cannot be empty";
        }

        if (searxUrl == null || searxUrl.isBlank()) {
            return "Error: SearXNG URL is required";
        }

        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = searxUrl + "/search?q=" + encodedQuery + "&format=json";

            String response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(15))
                    .block();

            if (response == null) {
                return "No results from SearXNG";
            }

            return parseSearxResults(response, numResults);

        } catch (Exception e) {
            logger.error("SearXNG search failed", e);
            return "SearXNG search failed: " + e.getMessage();
        }
    }

    private String parseSearxResults(String json, int numResults) throws Exception {
        JsonNode root = objectMapper.readTree(json);
        JsonNode results = root.get("results");

        if (results == null || !results.isArray()) {
            return "No results found";
        }

        List<SearchResult> searchResults = new ArrayList<>();
        int count = Math.min(results.size(), numResults);

        for (int i = 0; i < count; i++) {
            JsonNode result = results.get(i);
            SearchResult sr = new SearchResult();
            sr.setTitle(result.has("title") ? result.get("title").asText() : "No title");
            sr.setUrl(result.has("url") ? result.get("url").asText() : "");
            sr.setSnippet(result.has("content") ? result.get("content").asText() : "");
            searchResults.add(sr);
        }

        return formatResults(searchResults);
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SearchResult {
        private String title;
        private String url;
        private String snippet;
    }
}
