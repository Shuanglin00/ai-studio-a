# MCP 消息分类标注系统

基于 Spring AI MCP 的消息自动分类和手动标注系统。

## 功能概述

系统监听机器人发送的图片/合并转发消息，自动收集后续50条群员消息，基于关键词进行分类，支持手动标注修正。

## 架构设计

```
┌─────────────────────────────────────────────────────────────────┐
│                         整体架构                                 │
└─────────────────────────────────────────────────────────────────┘

  ┌──────────────┐         ┌──────────────┐         ┌──────────────┐
  │   bot模块     │         │   mcp模块     │         │  dbModel模块  │
  │              │ ──────▶ │              │ ──────▶ │              │
  │ • 消息监听    │         │ • Mock数据    │         │ • 实体定义    │
  │ • 指令处理    │         │ • 自动分类    │         │ • Repository │
  │ • 消息收集    │         │ • 手动标注    │         │ • 枚举定义    │
  └──────────────┘         └──────────────┘         └──────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                        数据流转                                  │
└─────────────────────────────────────────────────────────────────┘

  1. 触发阶段
     机器人发送图片/转发消息 ──▶ 创建收集批次 ──▶ 状态: COLLECTING

  2. 收集阶段 (5秒延迟)
     收集后续50条消息 ──▶ 更新批次 ──▶ 状态: COMPLETED

  3. 分类阶段
     提取消息内容 ──▶ 关键词匹配 ──▶ 计算置信度 ──▶ 保存结果

  4. 标注阶段 (可选)
     用户引用消息 ──▶ 发送 #标记 [分类] ──▶ 更新分类结果
```

## 分类体系

| 编码 | 名称 | 说明 | 敏感 |
|------|------|------|------|
| meme | 弔图 | 搞笑图片、表情包 | 否 |
| hell-joke | 地狱笑话 | 黑色幽默、死亡相关 | 是 |
| regional-black | 地域黑 | 地域歧视言论 | 是 |
| political | 政治敏感 | 政治人物、政策讨论 | 是 |
| nsfw | NSFW | 不适宜工作场所内容 | 是 |
| normal | 正常 | 普通聊天内容 | 否 |
| spam | 刷屏 | 无意义重复内容 | 否 |
| ad | 广告 | 商业广告、推广信息 | 否 |
| other | 其他 | 未分类内容 | 否 |

## MCP Tools

### 1. Mock数据生成工具 (MockDataGeneratorTool)

```java
@Tool(name = "generateMockMessageBatch")
String generateMockMessageBatch(
    String groupId,      // 群号
    int messageCount,    // 消息数量(1-50)
    String category      // 分类: MEME/HELL_JOKE/NORMAL/SPAM/AD
)
```

**用途**: 生成模拟消息批次用于测试

**示例**:
```json
{
  "groupId": "123456789",
  "messageCount": 10,
  "category": "MEME"
}
```

### 2. 消息存储工具 (MessageStorageTool)

```java
// 保存批次
@Tool(name = "saveMessageBatch")
String saveMessageBatch(String batchJson)

// 查询批次
@Tool(name = "getBatchById")
String getBatchById(String batchId)

// 按群号查询
@Tool(name = "getBatchesByGroup")
String getBatchesByGroup(String groupId)

// 更新分类(手动标注)
@Tool(name = "updateClassification")
boolean updateClassification(String batchId, String categoryCode, Long userId)
```

### 3. 分类管理工具 (CategoryManagementTool)

```java
// 列出所有分类
@Tool(name = "listCategories")
String listCategories()

// 获取分类详情
@Tool(name = "getCategoryDetails")
String getCategoryDetails(String categoryCode)

// 验证分类有效性
@Tool(name = "isValidCategory")
boolean isValidCategory(String categoryCode)

// 判断是否敏感分类
@Tool(name = "isSensitiveCategory")
boolean isSensitiveCategory(String categoryCode)
```

### 4. 消息分类工具 (MessageClassificationTool)

```java
// 自动分类
@Tool(name = "classifyMessageBatch")
String classifyMessageBatch(String batchJson)
```

**分类算法**: 基于关键词匹配
- 遍历消息内容，匹配各分类关键词库
- 统计命中次数，得分最高者为主分类
- 置信度 = 0.5 + (命中次数 * 0.1)，上限0.95

**返回格式**:
```json
{
  "primaryCategory": "meme",
  "categoryName": "弔图",
  "confidence": 0.85,
  "reasoning": "检测到3个meme相关关键词"
}
```

## 使用指南

### 自动分类流程

```java
// 1. 生成Mock数据（测试）
String batchJson = mockDataGeneratorTool.generateMockMessageBatch(
    "123456789", 10, "MEME"
);

// 2. 保存批次
String batchId = messageStorageTool.saveMessageBatch(batchJson);

// 3. 自动分类
String result = messageClassificationTool.classifyMessageBatch(batchJson);
// 返回: {"primaryCategory":"meme","confidence":0.8,...}
```

### 手动标注流程

```
用户操作:
1. 引用要标记的消息
2. 发送: #标记 弔图

机器人响应:
已标记为【meme】
```

**支持的分类别名**:
- 弔图/meme/搞笑 → meme
- 地狱笑话/hell-joke/地狱 → hell-joke
- 地域黑/regional-black/地域 → regional-black
- 正常/normal → normal
- 广告/ad → ad
- 刷屏/spam → spam

### 查看帮助

发送: `#标记帮助`

## 数据模型

### MessageCollectionBatch (消息收集批次)

```java
{
  "id": "批次ID",
  "groupId": "群号",
  "triggerMessageId": 触发消息ID,
  "triggerMessageType": "IMAGE/FORWARD",
  "triggerContent": "触发内容摘要",
  "messages": [CollectedMessage],
  "messageCount": 50,
  "collectStartTime": "2024-01-01T12:00:00",
  "collectEndTime": "2024-01-01T12:05:00",
  "status": "COLLECTING/COMPLETED/CLASSIFIED/FAILED"
}
```

### MessageClassification (分类结果)

```java
{
  "id": "分类ID",
  "batchId": "批次ID",
  "groupId": "群号",
  "triggerMessageId": 触发消息ID,
  "category": "meme",
  "subCategories": ["normal"],
  "confidence": 0.95,
  "reasoning": "分类理由",
  "source": "AUTO/MANUAL",
  "classifiedBy": 用户ID,
  "classifiedAt": "2024-01-01T12:10:00"
}
```

## 配置

### application-mcp.yaml

```yaml
mcp:
  classification:
    enabled: true
    batch-size: 50
    delay-seconds: 5
    keywords:
      meme: ["哈", "笑", "图", "梗", "草", "抽象"]
      hell-joke: ["地狱", "死", "坟", "功德", "木鱼"]
      regional-black: ["你们那", "听说你们", "经典", "地域"]
```

## 测试

### 运行测试

```bash
# 全部测试
mvn test -pl dbModel,mcp

# dbModel模块
mvn test -pl dbModel

# MCP模块
mvn test -pl mcp
```

### 测试覆盖率

| 模块 | 测试数 | 覆盖率 |
|------|--------|--------|
| dbModel | 96 | 80%+ |
| mcp | 41 | 75%+ |

### 主要测试类

- `MessageCategoryTest` - 分类枚举测试 (48个)
- `BatchStatusTest` - 批次状态枚举测试 (18个)
- `MockDataGeneratorToolTest` - Mock生成工具测试 (17个)
- `CategoryManagementToolTest` - 分类管理工具测试 (12个)
- `MessageStorageToolTest` - 存储工具测试 (8个)
- `MessageClassificationToolTest` - 分类工具测试 (4个)

## 开发计划

### 已完成 ✓

- [x] 数据模型设计
- [x] Repository接口
- [x] MCP工具实现
- [x] 关键词分类算法
- [x] 手动标注指令
- [x] 单元测试

### 待实现

- [ ] 集成LLM分类（替换关键词算法）
- [ ] 群聊消息实时收集
- [ ] 分类结果统计报表
- [ ] Web管理界面
- [ ] 定时清理过期数据

## 贡献指南

### 添加新分类

1. 在 `MessageCategory` 枚举中添加
2. 在 `CategoryManagementTool` 中添加关键词
3. 更新分类描述文档

### 优化分类算法

修改 `MessageClassificationTool.classifyByKeywords()` 方法，可接入LLM实现更智能的分类。

## 许可证

MIT License
