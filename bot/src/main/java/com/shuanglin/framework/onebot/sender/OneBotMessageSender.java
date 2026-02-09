package com.shuanglin.framework.onebot.sender;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.shuanglin.framework.onebot.config.OneBotApiProperties;
import com.shuanglin.framework.onebot.config.RetryProperties;
import com.shuanglin.framework.onebot.model.Message;
import com.shuanglin.framework.onebot.model.MessageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * OneBot消息发送器实现
 * 支持重试机制和消息确认
 */
@Slf4j
@Component
public class OneBotMessageSender implements MessageSender {

    private final OneBotApiProperties oneBotApiProperties;
    private final RetryProperties retryProperties;
    private final RestTemplate restTemplate;
    private final Gson gson;
    private final RetryPolicy retryPolicy;

    public OneBotMessageSender(OneBotApiProperties oneBotApiProperties, RetryProperties retryProperties) {
        this.oneBotApiProperties = oneBotApiProperties;
        this.retryProperties = retryProperties;
        this.restTemplate = new RestTemplate();
        this.gson = new Gson();
        this.retryPolicy = new RetryPolicy(retryProperties);
    }

    @Override
    public MessageResponse sendGroupMessage(Message message) {
        String url = oneBotApiProperties.getBaseUrl() + "/send_group_msg";

        // 构建请求体
        JsonObject body = new JsonObject();
        body.addProperty("group_id", message.getTargetId());
        body.add("message", message.toJsonArray());

        return executeWithRetry(url, body);
    }

    @Override
    public MessageResponse sendPrivateMessage(Message message) {
        String url = oneBotApiProperties.getBaseUrl() + "/send_private_msg";

        // 构建请求体
        JsonObject body = new JsonObject();
        body.addProperty("user_id", message.getTargetId());
        body.add("message", message.toJsonArray());

        return executeWithRetry(url, body);
    }

    /**
     * 执行带重试的HTTP POST请求
     */
    private MessageResponse executeWithRetry(String url, JsonObject body) {
        return retryPolicy.execute(() -> executeHttpPost(url, body));
    }

    /**
     * 执行HTTP POST请求
     */
    private MessageResponse executeHttpPost(String url, JsonObject body) {
        log.debug("Sending message to OneBot API: {}", url);
        log.debug("Request body: {}", body);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(body.toString(), headers);
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                String.class
        );

        // 解析响应
        JsonObject responseJson = gson.fromJson(response.getBody(), JsonObject.class);

        MessageResponse messageResponse = MessageResponse.builder()
                .status(responseJson.has("status") ? responseJson.get("status").getAsString() : "ok")
                .retcode(responseJson.has("retcode") ? responseJson.get("retcode").getAsInt() : 0)
                .build();

        if (responseJson.has("data")) {
            JsonObject data = responseJson.getAsJsonObject("data");
            if (data.has("message_id")) {
                messageResponse.setMessageId(data.get("message_id").getAsLong());
            }
        }

        log.info("Message sent successfully, messageId={}", messageResponse.getMessageId());
        return messageResponse;
    }
}
