package com.shuanglin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class NekoQADatasetDownloadTest {

    private static final String DATASET_URL = "https://datasets-server.huggingface.co/rows?dataset=liumindmind%2FNekoQA-10K&config=default&split=train&offset=0&length=100";
    private static final String OUTPUT_DIR = "src/main/resources/file/dataset";

    @Test
    void downloadDatasetToResources() throws Exception {
        // 确保输出目录存在
        Path outputPath = Paths.get(OUTPUT_DIR);
        Files.createDirectories(outputPath);

        // 下载数据
        String jsonContent = downloadJson(DATASET_URL);
        assertNotNull(jsonContent, "下载内容不应为空");

        // 解析并保存数据
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(jsonContent);
        JsonNode rowsNode = rootNode.path("rows");

        // 保存完整JSON文件
        Path outputFile = outputPath.resolve("nekoqa_train_0_100.json");
        Files.writeString(outputFile, mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rowsNode));

        assertTrue(Files.exists(outputFile), "文件应已创建");
        assertTrue(Files.size(outputFile) > 0, "文件大小应大于0");

        System.out.println("数据集已下载到: " + outputFile.toAbsolutePath());
        System.out.println("总记录数: " + rowsNode.size());
    }

    private String downloadJson(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");

        try (InputStream inputStream = connection.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        } finally {
            connection.disconnect();
        }
    }
}
