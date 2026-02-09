# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working code in this repository.

## Project Overview

Multi-module Maven project (Java 17, Spring Boot 3.4.5) that implements an AI-powered bot with RAG capabilities. Uses LangChain4j for AI integration, supporting Ollama, Gemini, Qwen, and MiniMax models.

The project has evolved through multiple versions:
- **V1**: Session-based AI interaction
- **V2**: Session-less data storage
- **V3**: Hybrid session + session-less storage
- **V4**: Multi-step RAG (uses knowledge base for sub-query retrieval without requiring session history)

## Build Commands

```bash
# Build entire project
mvn clean install

# Build specific module
mvn clean install -pl ai
mvn clean install -pl bot
mvn clean install -pl dbModel -pl common

# Build without tests
mvn clean install -DskipTests

# Run a specific test class
mvn test -Dtest=TestNovelGraphBuild
```

## Run Commands

```bash
# Run from bot module
mvn spring-boot:run -pl bot

# Or run main class directly
java -jar bot/target/bot-1.0.jar
```

Main entry points:
- `bot/src/main/java/StartBot.java`
- `bot/src/main/java/BotStart.java`

## Architecture

### Multi-Module Structure

| Module | Purpose | Dependencies |
|--------|---------|--------------|
| `common` | Shared utilities, constants, enums | - |
| `dbModel` | MongoDB/Milvus/Neo4j entities and DAOs | common |
| `ai` | AI processing, RAG, embeddings | dbModel, common |
| `bot` | Event-driven message handling framework | ai |
| `train` | ML model training and inference (DJL-based) | dbModel, common |

### Key Patterns

1. **Event Bus Pattern** (`bot/framework/bus/`): Reactor-based `MessageBus` using Sinks.Many for decoupled message handling with `@PublishBus` and `@GroupMessageHandler` annotations. Handlers are registered in `MethodRegistry` and processed via `MessageHandlerAspect`.

2. **Permission System** (`bot/framework/permission/`): Three-tier permission architecture using Spring events:
   - **GlobalPermission**: Command-level default configuration (stored in `global_permissions` collection)
   - **GroupPermission**: Per-group command settings like blacklist/whitelist/disabled (stored in `group_permissions` collection)
   - **UserPermission**: User-specific overrides (stored in `user_permissions` collection)

   **Initialization Flow**:
   ```
   CommandRegistry (onApplicationEvent)
        │ 扫描命令完成
        ▼
   publishCommandRegistryReadyEvent()
        │ 发布 Spring 事件
        ▼
   PermissionManager (onCommandRegistryReady)
        │ 监听事件
        ▼
   加载/创建 GlobalPermission → 为已有群聊创建 GroupPermission
   ```

   **Auto-Initialization**:
   - Group permissions are auto-created on first access (Redis → DB → default enabled)
   - New commands automatically get GroupPermission records for all groups that already have permissions
   - Use `disableCommandInGroup(groupId, commandName)` / `enableCommandInGroup(groupId, commandName)` to toggle

3. **RAG System** (`ai/langchain4j/rag/`): Multi-step query decomposition with:
   - `MultiStepQueryRetriever` for query rewriting
   - `FilterMemoryStore` combining MongoDB + Milvus for context retrieval
   - Neo4j knowledge graph for novel entity relationships

3. **LangChain4j Assistants** (`ai/langchain4j/assistant/`): Declarative AI services using `@UserMessage` annotation (e.g., `GeminiAssistant`, `OllamaAssistant`, `MiniMaxAssistant`). Auto-configured via `ApiModelsConfiguration`.

4. **Memory Stores** (`ai/langchain4j/store/`): `FilterMemoryStore`, `MultiStepMemoryStore`, `NonMemoryStore` for context management.

5. **DJL Model Framework** (`train/`): Local ML model inference using Deep Java Library:
   - Multiple providers: GGUF, HuggingFace, ONNX
   - Model types: TextModel, EmbeddingModel, ImageModel, RerankModel
   - Declaration-based bean registration via `@Configuration` + `@Bean`
   - Configuration via `application.yaml` with automatic bean-to-config mapping

### DJL Train Module Configuration

```yaml
train:
  model:
    cache-enabled: true
    cache-dir: ./model-cache
    max-models: 10
    models:
      qwen-huggingface:
        name: qwen-huggingface
        type: textModel
        framework: huggingface
        model-path: Qwen/Qwen2.5-7B-Instruct
        device: auto
        threads: 4

      bge-huggingface:
        name: bge-huggingface
        type: embeddingModel
        framework: huggingface
        model-path: BAAI/bge-large-zh-v1.5
```

### DJL Model Injection Examples

```java
// Inject default model (uses @Primary)
@Autowired
private TextModel textModel;

// Inject specific model by bean name
@Autowired
@Qualifier("huggingfaceTextModel")
private HuggingfaceTextModel huggingfaceModel;

// Use ModelProvider for programmatic access
@Autowired
private ModelProvider modelProvider;

private void example() {
    TextModel text = modelProvider.getTextModel();
    Optional<EmbeddingModel> embedding = modelProvider.getEmbeddingModel("huggingfaceEmbeddingModel");
}
```

### Data Layer

- **MongoDB**: Message/session storage (`MessageStoreEntity`)
- **Milvus**: Vector embeddings (`MessageEmbeddingEntity`)
- **Neo4j**: Knowledge graph nodes/relationships (CharacterNode, ChapterNode, EntityNode)
- **Redis**: Caching (see `bot/config/RedisConfig`)

### Milky API Client

The `milky` module (`bot/framework/milky/`) provides a typed client for OneBot/Milky API:

```
bot/src/main/java/com/shuanglin/framework/milky/
├── config/MilkyApiConfig.java       # API configuration
├── client/
│   ├── MilkyApiClient.java          # Main API client
│   └── MessageBuilder.java          # Message segment builder
├── enums/                           # Enums (Sex, GroupRole, MessageType, etc.)
└── model/
    ├── request/                     # Request DTOs (55 classes)
    └── response/                    # Response DTOs (40 classes)
```

#### Configuration

```yaml
milky:
  base-url: http://127.0.0.1:3000   # OneBot API server
  access-token:                     # Optional API token
  timeout: 5000                     # Request timeout (ms)
  connect-timeout: 3000             # Connection timeout (ms)
  read-timeout: 5000                # Read timeout (ms)
```

#### Usage Example

```java
@Autowired
private MilkyApiClient milkyApiClient;

// Send group message with @mention
MessageResponse resp = milkyApiClient.sendGroupMessage(
    123456789L,
    MessageBuilder.create()
        .text("Hello ")
        .at(987654321L)
        .build()
);

// Get group member list
GroupMemberListResponse members = milkyApiClient.getGroupMemberList(123456789L, false);
```

#### API Categories

- **System API**: Login info, user profile, cookies, CSRF token
- **Message API**: Send/recall messages, get message history
- **Friend API**: Nudge, stranger info
- **Group API**: Announcements, honor, management (mute, kick, admin, etc.)
- **File API**: Upload/download, folder management, group links

## Configuration Files

- `config/` - External configuration directory (NOT tracked by git, contains sensitive info)
- `ai/src/main/resources/application.yaml`: AI module config (server port 8080, DB connections)
- `bot/src/main/resources/application.yaml`: Bot module config
- `train/src/main/resources/application.yaml`: Train module config (model definitions)

## Maven Profiles & Configuration Management

The project uses Maven profiles to manage configurations for different environments:

### Directory Structure
```
config/                    # External config directory (git-ignored)
├── common/                # Common configuration shared by all environments
│   └── application.yaml
├── local/                 # Local development environment
│   └── application.yaml
├── dev/                   # Development environment (default)
│   └── application.yaml
├── sit/                   # System Integration Testing
│   └── application.yaml
└── prod/                  # Production environment
    └── application.yaml
```

### Build Commands by Environment

```bash
# Local development (uses external config/, no module config packaged)
mvn clean install -Plocal

# Development environment (default)
mvn clean install

# SIT environment
mvn clean install -Psit

# Production environment
mvn clean install -Pprod
```

### Configuration Priority (High to Low)
1. `config/{profile}/` - Environment-specific external config
2. `config/common/` - Common external config
3. `module/src/main/resources/` - Module internal config (fallback)

### Local Profile Behavior
When using `-Plocal`:
- Module internal `application.yaml/yml/properties` are NOT packaged
- Only external config from `config/local/` and `config/common/` is used
- Useful for:
  - Keeping sensitive config outside of git
  - Different developers can have different local configs
  - CI/CD can inject environment-specific configs

### Environment Variables
Use `${VAR_NAME:default}` syntax in config files:
```yaml
mongodb:
  uri: ${MONGODB_URI:mongodb://localhost:27017/catBot}
api-key: ${GEMINI_API_KEY:your-default-key}
```

## Test Location

Tests are in `ai/src/test/java/` including:
- `TestNovelGraphBuild.java`: Novel knowledge graph building tests
- `TestGraphServiceMemory.java`: GraphService memory tests
- `KnowledgeGraphChapterGeneration.java`: Knowledge graph chapter generation
- `DjlModelUsageExample.java`: DJL model usage examples (in `ai/src/main/java/`)

## Neo4j Knowledge Graph

The novel knowledge graph (`com.shuanglin.dao.neo4j.novel`) models:
- **Entities**: Characters, Locations, Organizations, Items, Skills (all extend `EntityNode`)
- **Events**: Chapter-level events with types (BATTLE, DIALOGUE, CULTIVATION, etc.)
- **States**: Time-sliced entity state snapshots (REALM, HEALTH, LOCATION, etc.)
- **Chapters & Summaries**: Content organization

See `.qoder/quests/novel-timeline-knowledge-graph.md` for detailed design documentation including a 4-stage precision reconstruction workflow for sentence-level novel content analysis.

## Lessons Learned (Bug Fixes)

### GSON Deserialization Issue with Message Segments

**Problem**: `MessageEvent.getMessageText()` returned empty string for array-formatted messages.

**Root Cause**:
- OneBot Webhook sends `message` as JSON array: `[{"type":"text","data":{"text":"渚"}}]`
- GSON deserializes array elements as `JsonObject` or `LinkedTreeMap`, NOT as custom POJO classes like `TextSegment`
- The code checked `segment instanceof TextSegment` which always returned false

**Fix**: Use GSON's `JsonObject` API directly instead of relying on POJO deserialization:
```java
if (segment instanceof JsonObject) {
    JsonObject obj = (JsonObject) segment;
    JsonElement typeElem = obj.get("type");
    if (typeElem != null && "text".equals(typeElem.getAsString())) {
        JsonElement textElem = obj.getAsJsonObject("data").get("text");
        if (textElem != null) {
            sb.append(textElem.getAsString());
        }
    }
}
```

### Reactor Stream Duplicate Execution

**Problem**: Command handlers executed twice per message.

**Root Cause**:
- `doOnNext()` called `processMessage()` AND the `subscribe()` consumer also called it
- Reactor propagates events through the entire stream, causing double execution

**Fix**: Remove `doOnNext()` and only call processing in the subscribe consumer:
```java
messageBus.getBus()
    .filter(...)
    .publishOn(Schedulers.boundedElastic())
    .subscribe(
        event -> processMessage(...),  // Only here
        error -> log.error(...)
    );
```

### MongoDB Duplicate Data on Startup

**Problem**: `CommandRegistry.syncCommandsToDatabase()` inserted duplicate commands on every startup.

**Root Cause**:
- Used `save()` without checking if command already exists
- MongoDB allows multiple documents with same `commandName` field

**Fix**: Delete existing commands before inserting:
```java
for (CommandInfo existingCmd : commandInfoList) {
    commandRepository.deleteByCommandName(existingCmd.getCommandName());
}
for (CommandInfo cmdInfo : commandInfoList) {
    commandRepository.save(new Command(...));
}
```
