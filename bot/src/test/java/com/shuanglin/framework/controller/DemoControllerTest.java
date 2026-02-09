package com.shuanglin.framework.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * DemoController 单元测试
 */
class DemoControllerTest {

    private MockMvc mockMvc;

    private void setupMockMvc() {
        mockMvc = MockMvcBuilders.standaloneSetup(new DemoController())
                .addFilters(new CharacterEncodingFilter(StandardCharsets.UTF_8.name(), true))
                .setMessageConverters(
                        new StringHttpMessageConverter(StandardCharsets.UTF_8),
                        new ByteArrayHttpMessageConverter(),
                        new MappingJackson2HttpMessageConverter()
                )
                .build();
    }

    @Test
    @DisplayName("测试控制器返回原始消息内容")
    void testControllerReturnsPayload() throws Exception {
        setupMockMvc();

        String messageJson = "{\"self_id\":2784152733,\"user_id\":1751649231,\"message_type\":\"group\",\"group_id\":345693826,\"raw_message\":\"test\",\"post_type\":\"message\"}";

        mockMvc.perform(post("/bot")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(messageJson.getBytes(StandardCharsets.UTF_8)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("test")));
    }

    @Test
    @DisplayName("测试控制器处理中文字符")
    void testControllerWithChineseCharacter() throws Exception {
        setupMockMvc();

        String messageJson = "{\"self_id\":2784152733,\"user_id\":1751649231,\"message_type\":\"group\",\"group_id\":345693826,\"raw_message\":\"渚\",\"post_type\":\"message\"}";

        mockMvc.perform(post("/bot")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(messageJson.getBytes(StandardCharsets.UTF_8)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("渚")));
    }

    @Test
    @DisplayName("测试控制器处理多媒体消息")
    void testControllerWithMediaMessage() throws Exception {
        setupMockMvc();

        // 包含图片 base64 的多媒体消息
        String messageJson = "{\"self_id\":2784152733,\"user_id\":1751649231,\"message_type\":\"group\",\"group_id\":345693826,\"raw_message\":\"[CQ:image,file=xxx.jpg]\",\"post_type\":\"message\",\"message\":[{\"type\":\"image\",\"data\":{\"file\":\"base64://abc123\",\"url\":\"http://example.com/image.jpg\"}}]}";

        mockMvc.perform(post("/bot")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(messageJson.getBytes(StandardCharsets.UTF_8)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("base64://abc123")));
    }
}
