import dev.langchain4j.model.openai.OpenAiChatModel;

public class MiniMaxExample {
    public static void main(String[] args) {
        // 创建MiniMax模型客户端
        OpenAiChatModel client = OpenAiChatModel.builder()
            .baseUrl("https://api.minimaxi.com/v1")
            .apiKey("your_api_key")
            .modelName("MiniMax-M2")
            .temperature(0.7)
            .logRequests(true)
            .logResponses(true)
            .build();

        // 发送请求
        String response = client.chat("Hi, how are you?");

        System.out.println("Response: " + response);
    }
}