# API端点参考

<cite>
**Referenced Files in This Document**   
- [ChatController.java](file://ai/src/main/java/com/shuanglin/bot/langchain4j/controller/ChatController.java)
- [DocumentInitializer.java](file://ai/src/main/java/com/shuanglin/bot/langchain4j/config/DocumentInitializer.java)
- [OllamaAssistant.java](file://ai/src/main/java/com/shuanglin/bot/langchain4j/assistant/OllamaAssistant.java)
</cite>

## 目录
1. [/chat/ask 端点](#chatask-端点)
2. [/chat/readFile 端点](#chatreadfile-端点)
3. [/chat/read 端点](#chatread-端点)

## /chat/ask 端点

该端点是与AI模型进行对话的核心接口，接收用户的提问并返回生成的回答。

**HTTP方法**: `POST`  
**URL路径**: `/chat/ask`

### 请求参数
- **请求体 (Body)**:
  - **类型**: `application/json`
  - **数据结构**:
    ```json
    {
      "type": "object",
      "properties": {
        "message": {
          "type": "string",
          "description": "用户输入的原始问题文本。"
        }
      },
      "required": ["message"],
      "additionalProperties": true
    }
    ```
  - **说明**: 请求体是一个JSON字符串，其中必须包含`message`字段。其他字段（如会话ID、用户ID等）可以作为附加属性存在，将被传递给AI模型。

### 响应
- **状态码**: `200 OK`
  - **响应体**: `string`
  - **说明**: 成功时，返回一个包含AI生成回答的字符串。
- **状态码**: `500 Internal Server Error`
  - **说明**: 当AI模型处理请求时发生内部错误（例如，与Ollama服务通信失败）。

### 业务逻辑
1.  接收包含用户问题的JSON字符串。
2.  使用`Gson`库将JSON字符串解析为`JsonObject`对象。
3.  为本次请求生成一个唯一的`messageId`并添加到参数中，用于日志追踪。
4.  调用`OllamaAssistant`服务的`chat`方法，将解析后的参数和用户消息传递给它。
5.  将`OllamaAssistant`返回的回答原样作为HTTP响应返回给客户端。

### 使用场景
此端点用于实现标准的聊天机器人功能，前端应用可以将用户的输入封装成指定格式的JSON，发送到此端点，并将返回的字符串显示给用户。

### 请求示例
```http
POST /chat/ask HTTP/1.1
Content-Type: application/json

{
  "message": "你好，今天天气怎么样？",
  "userId": "12345"
}
```

### 响应示例
```http
HTTP/1.1 200 OK
Content-Type: text/plain;charset=UTF-8

你好！我无法获取实时天气信息，建议你查看天气预报应用。
```

**Section sources**
- [ChatController.java](file://ai/src/main/java/com/shuanglin/bot/langchain4j/controller/ChatController.java#L32-L39)
- [OllamaAssistant.java](file://ai/src/main/java/com/shuanglin/bot/langchain4j/assistant/OllamaAssistant.java#L26-L28)

## /chat/readFile 端点

该端点用于上传文件，将文件内容（如小说、文档）解析并存储到向量数据库中，以构建知识库，支持后续的检索增强生成（RAG）。

**HTTP方法**: `POST`  
**URL路径**: `/chat/readFile`

### 请求参数
- **请求体 (Body)**:
  - **类型**: `multipart/form-data`
  - **表单字段**:
    - `file` (必需)
      - **类型**: `file`
      - **说明**: 要上传的文件。支持单个文本文件（如`.txt`, `.java`, `.md`）或包含多个文本文件的ZIP压缩包。

### 响应
- **状态码**: `200 OK`
  - **响应体**: `空`
  - **说明**: 文件上传和处理成功。
- **状态码**: `400 Bad Request`
  - **说明**: 上传的文件为空或未提供。
- **状态码**: `500 Internal Server Error`
  - **说明**: 处理文件时发生内部错误（例如，读取文件、生成向量或存储数据失败）。

### 业务逻辑
1.  接收一个名为`file`的`MultipartFile`。
2.  检查文件是否存在且不为空。
3.  获取文件名和后缀，并创建一个临时文件。
4.  将上传的文件内容写入临时文件。
5.  调用`DocumentInitializer`服务的`readFile`方法，传入一个空的`JsonObject`和临时文件。
6.  `DocumentInitializer`会根据文件类型进行处理：
    - **单个文件**: 直接读取文件内容，生成向量，并存入Milvus向量数据库和MongoDB。
    - **ZIP文件**: 解压并遍历其中所有文本文件，对每个文件执行与单个文件相同的处理流程。
7.  处理完成后，临时文件会被自动清理。

### 使用场景
此端点用于知识库的构建。用户可以上传小说文本、技术文档等，系统会自动将其内容向量化并存储，使得AI在后续的问答中能够基于这些上传的知识进行回答。

### 请求示例
```http
POST /chat/readFile HTTP/1.1
Content-Type: multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW

------WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="file"; filename="novel.txt"
Content-Type: text/plain

[小说文本内容...]
------WebKitFormBoundary7MA4YWxkTrZu0gW--
```

### 响应示例
```http
HTTP/1.1 200 OK
```

**Section sources**
- [ChatController.java](file://ai/src/main/java/com/shuanglin/bot/langchain4j/controller/ChatController.java#L41-L58)
- [DocumentInitializer.java](file://ai/src/main/java/com/shuanglin/bot/langchain4j/config/DocumentInitializer.java#L62-L84)

## /chat/read 端点

该端点用于将一段纯文本字符串直接作为知识库内容进行处理和存储，适用于动态生成或从其他来源获取的文本。

**HTTP方法**: `POST`  
**URL路径**: `/chat/read`

### 请求参数
- **请求体 (Body)**:
  - **类型**: `application/json`
  - **数据结构**: `string`
  - **说明**: 需要被处理和存储的原始文本字符串。

### 响应
- **状态码**: `200 OK`
  - **响应体**: `string`
  - **内容**: `"OK"`
  - **说明**: 文本处理和存储成功。
- **状态码**: `500 Internal Server Error`
  - **说明**: 处理文本时发生内部错误。

### 业务逻辑
1.  接收一个包含文本内容的字符串。
2.  在代码中硬编码创建一个`JsonObject`，并设置`userId`和`modelName`字段。
3.  调用`DocumentInitializer`服务的`read`方法，传入创建的`JsonObject`和接收到的文本字符串。
4.  `DocumentInitializer`会调用`processSingleContent`方法，将该字符串视为一个独立的文档单元，生成向量并存入Milvus和MongoDB。

### 使用场景
此端点适用于需要将非文件形式的文本（例如，从网页抓取的内容、API返回的摘要）快速添加到知识库的场景。它提供了一种比上传文件更轻量级的接口。

### 请求示例
```http
POST /chat/read HTTP/1.1
Content-Type: application/json

"这是一个关于人工智能的简短介绍。人工智能是计算机科学的一个分支，旨在创造能够模拟人类智能行为的机器。"
```

### 响应示例
```http
HTTP/1.1 200 OK
Content-Type: text/plain;charset=UTF-8

OK
```

**Section sources**
- [ChatController.java](file://ai/src/main/java/com/shuanglin/bot/langchain4j/controller/ChatController.java#L60-L67)
- [DocumentInitializer.java](file://ai/src/main/java/com/shuanglin/bot/langchain4j/config/DocumentInitializer.java#L91-L94)