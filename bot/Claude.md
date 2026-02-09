# Claude Code - Bot 模块

## 项目概述

Bot 模块是一个基于 Spring Boot 和 OneBot 协议的 QQ 机器人框架，支持群聊和私聊消息处理、权限管理、AI 问答等功能。

## 核心架构

### 消息流转

```
OneBot Webhook → DemoController (/bot) → MessageBus → 消息监听器 → 执行器 → OneBot API
```

### 关键组件

| 组件 | 路径 | 说明 |
|------|------|------|
| 消息总线 | `framework/bus/MessageBus.java` | 事件发布/订阅机制 (Reactor) |
| AOP 切面 | `framework/aop/PublishToBusAspect.java` | 自动将 @PublishBus 方法返回值发布到总线 |
| 消息监听器 | `framework/listener/` | 群聊/私聊消息监听 |
| 执行器 | `executor/` | 消息处理逻辑实现 |
| OneBot API | `framework/onebot/` | 消息发送协议封装 |
| 权限管理 | `framework/permission/` | 用户权限校验 |

### @PublishBus 注解

使用 `@PublishBus` 注解标记 Controller 方法，自动将其返回值发布到消息总线：

```java
@PostMapping(value = "/bot")
@PublishBus
public String handleWebhook(@RequestBody byte[] body) {
    return new String(body, StandardCharsets.UTF_8);
}
```

**工作原理**：
1. Controller 接收 OneBot Webhook 请求，返回原始 JSON 字符串
2. `PublishToBusAspect` AOP 拦截带有 `@PublishBus` 注解的方法
3. 解析 JSON 字符串为 `Event` 或 `GroupMessageEvent` 对象
4. 设置 `rawData` 存储原始字段值（用于 `event.get(key)` 方法）
5. 发布到 `MessageBus`

**Event 类型判断**：
- `post_type=message` + `message_type=group` → `GroupMessageEvent`
- 其他情况 → `Event`

### 消息总线架构

```
Controller 返回 JSON 字符串
        ↓
@PublishBus AOP 拦截
        ↓
parseEvent() 解析并设置 rawData
        ↓
MessageBus.publish(Event)
        ↓
Reactor Sinks.Many<Event>
        ↓
GroupMessageListener 订阅过滤
        ↓
processMessage() 分发到 Executor
```

**Event 类结构**：
- `Event` - 基类，包含 `time`、`selfId`、`postType`、`rawData`
- `MessageEvent` - 消息事件基类，继承自 `Event`，包含 `messageId`、`userId`、`groupId` 等
- `GroupMessageEvent` - 群消息事件，继承自 `MessageEvent`

**rawData 使用**：
```java
// 通过 event.get(key) 获取原始 JSON 字段值
boolean isGroup = "group".equals(event.get("message_type"));
String rawMsg = (String) event.get("raw_message");
```

## 配置文件

### 配置文件结构

项目使用统一的配置文件管理，配置文件位于项目根目录的 `config/` 目录下：

```
config/
├── application-common.yaml   # 通用配置（所有环境共享）
├── application-local.yaml    # Local 环境（本地开发）
├── application-dev.yaml      # Dev 环境（开发服务器）
├── application-sit.yaml      # SIT 环境（测试）
├── application-prod.yaml     # Prod 环境（生产）
└── logback-spring.xml        # 日志配置
```

### 配置文件优先级

1. `bot/src/main/resources/application.yaml` - 模块内默认配置
2. `config/application-{profile}.yaml` - 环境配置（可覆盖模块内配置）
3. `config/application-common.yaml` - 通用配置

### 主要配置项

```yaml
# OneBot API 配置
onebot:
  api:
    base-url: http://127.0.0.1:3000  # OneBot 服务器地址
    timeout: 5000                    # 超时时间（毫秒）
    retry:
      max-attempts: 3                # 最大重试次数
      delay-ms: 1000                 # 初始重试延迟
      multiplier: 2.0                # 延迟倍数（指数退避）

# Bot 框架配置
bot:
  framework:
    permission:
      enabled: true                  # 是否启用权限验证
  pig:
    image-path: ${user.dir}/bot/src/main/resources/pigs  # 猪图片目录
  log:
    path: ${user.dir}/../APPS/log    # 日志输出目录
    level: INFO                      # 日志级别
```

### 环境切换

```bash
# Local 环境
mvn -Plocal spring-boot:run

# Dev 环境（默认）
mvn -Pdev spring-boot:run

# SIT 环境
mvn -Psit spring-boot:run

# Prod 环境
mvn -Pprod spring-boot:run
```

### 日志配置

日志输出到 `/APPS/log/` 目录：
- `bot.log` - 普通日志
- `bot-error.log` - 错误日志（仅 ERROR 级别）

## 消息格式

### 群消息 (group)
```json
{
  "self_id": 2784152733,
  "user_id": 1751649231,
  "message_type": "group",
  "group_id": 345693826,
  "sender": {"user_id": 1751649231, "role": "owner", "title": ""},
  "raw_message": "123",
  "sub_type": "normal"
}
```

### 私聊消息 (private)
```json
{
  "self_id": 2784152733,
  "user_id": 1751649231,
  "message_type": "private",
  "sender": {"user_id": 1751649231},
  "raw_message": "123",
  "sub_type": "friend"
}
```

## 开发指南

### 添加新的消息处理器

1. 在 `executor/` 目录下创建执行器类
2. 使用 `@GroupMessageHandler` 注解标记处理方法
3. 使用 `GroupMessageBuilder` 构建和发送消息

```java
@Component
@Slf4j
public class MyExecutor {

    @GroupMessageHandler(triggerPrefix = "!hello", description = "发送问候")
    public void helloCommand(GroupMessageEvent group) {
        GroupMessageBuilder.forGroup(group.getGroupId())
                .text("你好！")
                .send();
    }
}
```

### 发送消息

```java
// 简单文本消息
GroupMessageBuilder.forGroup(groupId)
        .text("Hello")
        .send();

// @ 某用户
GroupMessageBuilder.forGroup(groupId)
        .at(userId)
        .text("Hello")
        .send();

// 图片消息
GroupMessageBuilder.forGroup(groupId)
        .addSegment(new ImageSegment("base64://..."))
        .send();
```

### 权限校验

通过 `PermissionValidator` 校验用户操作权限。

### 权限系统

Bot 使用三级权限架构：
1. **全局权限** (`GlobalPermission`) - 指令级别的默认配置
2. **群聊权限** (`GroupPermission`) - 特定群组的权限配置
3. **用户权限** (`UserPermission`) - 用户个人权限配置

#### 权限校验流程

```
用户发送指令 → 超级管理员检查 → 全局权限检查 → 群聊权限检查 → 用户权限检查 → 权限级别检查
```

#### 超级管理员

以下用户拥有最高权限，可调用所有指令：
- `1751649231`
- `2784152733`

超级管理员硬编码在 `RedisPermissionStore.java` 中：
```java
private static final Set<String> SUPER_ADMINS = Set.of(
        "1751649231",
        "2784152733"
);
```

#### 权限级别

| 级别 | 说明 |
|------|------|
| `PUBLIC` | 公开，任何人都可用 |
| `USER` | 仅限登录用户 |
| `GROUP_ADMIN` | 群管理员或群主 |
| `GROUP_OWNER` | 仅群主 |
| `BOT_ADMIN` | 机器人管理员 |
| `WHITELIST` | 仅白名单用户 |
| `BLACKLIST` | 黑名单除外 |

#### 角色权限

指令通过 `@GroupMessageHandler(role = "xxx")` 定义功能角色（用于菜单分类），与用户的 QQ 角色（member/admin/owner）不同。

默认情况下，所有用户角色均可使用指令。如需限制，可在 `GlobalPermission.allowedRoles` 中配置。

## 测试指南

### 运行测试

```bash
# 运行所有测试
mvn test

# 运行单个测试类
mvn test -Dtest=MenuServiceTest

# 运行特定测试方法
mvn test -Dtest=MenuServiceTest#testGetGlobalCommands
```

### 测试配置

测试使用 `@ExtendWith(MockitoExtension.class)` 进行单元测试。`RedisPermissionStoreTest` 需要 JVM 参数：
```xml
<argLine>--add-opens java.base/java.time=ALL-UNNAMED</argLine>
```

### MockMvc 测试

使用 `MockMvcBuilders.standaloneSetup()` 配合 `CharacterEncodingFilter` 处理中文编码：
```java
mockMvc = MockMvcBuilders.standaloneSetup(controller)
        .addFilters(new CharacterEncodingFilter(StandardCharsets.UTF_8.name(), true))
        .setMessageConverters(new StringHttpMessageConverter(StandardCharsets.UTF_8))
        .build();
```

## 特性

### 重试机制

OneBot 消息发送支持自动重试（指数退避）：
- 默认最大重试次数：3
- 默认初始延迟：1秒
- 延迟倍数：2x

### 消息确认

发送消息后返回 `MessageResponse`，包含：
- `status` - 状态
- `retcode` - 返回码
- `messageId` - 消息 ID（发送成功时）

## 启动入口

- `BotStart.java` - Spring Boot 启动类
