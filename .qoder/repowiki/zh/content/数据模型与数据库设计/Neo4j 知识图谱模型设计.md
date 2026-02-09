# Neo4j 知识图谱模型设计

<cite>
**Referenced Files in This Document**   
- [GraphService.java](file://ai/src/main/java/com/shuanglin/bot/service/GraphService.java)
- [ArticlesEntity.java](file://dbModel/src/main/java/com/shuanglin/dao/Articles/ArticlesEntity.java)
- [kgKnowlage.md](file://file/kgKnowlage.md)
- [IsolationMetadata.java](file://ai/src/main/java/com/shuanglin/bot/model/IsolationMetadata.java)
- [ChapterStorageService.java](file://ai/src/main/java/com/shuanglin/bot/service/ChapterStorageService.java)
</cite>

## 目录
1. [引言](#引言)
2. [节点与关系模型](#节点与关系模型)
3. [Cypher语句管理](#cypher语句管理)
4. [数据源与隔离](#数据源与隔离)
5. [构建流程](#构建流程)
6. [查询模式](#查询模式)
7. [知识图谱示意图](#知识图谱示意图)
8. [结论](#结论)

## 引言

本文档旨在详细阐述基于`GraphService`服务和`ArticlesEntity`实体的Neo4j知识图谱数据模型。该模型通过结合大型语言模型（LLM）的文本理解能力与图数据库的结构化存储优势，实现了从小说文本到结构化知识图谱的自动化构建。核心机制围绕`kgKnowlage.md`中定义的本体论框架，将文本中的实体、事件和状态变化转化为符合Cypher规范的图数据。本文将深入解析其节点与关系模型、Cypher语句的生命周期管理、多数据源隔离策略、完整的构建流程以及智能查询模式。

**Section sources**
- [GraphService.java](file://ai/src/main/java/com/shuanglin/bot/service/GraphService.java#L30-L706)
- [kgKnowlage.md](file://file/kgKnowlage.md#L0-L933)

## 节点与关系模型

知识图谱的核心是其本体论框架，它定义了构成图的节点类型、属性以及它们之间的关系。该模型严格遵循`kgKnowlage.md`文档中定义的三大核心节点：**事件 (Event)**、**实体 (Entity)** 和 **状态 (State)**。

### 核心节点类型

1.  **事件 (Event)**
    *   **定义**: 代表系统中状态变化的瞬时驱动者，是图中的“动词”。每个事件都发生在精确的时间点。
    *   **核心属性**:
        *   `uuid`: 全局唯一标识符。
        *   `timestamp`: DateTime类型，事件发生的精确时间。
        *   `eventType`: 事件类型，如 "Generation"（生成）、"Transformation"（转换）、"Termination"（终止）、"Observation"（观测）。
        *   `source`: 数据来源，格式为“第X章 章节名”。
        *   `chapterIndex`: 章节索引，用于排序和查询。
    *   **领域特化**: 通过多标签机制，事件可以特化为`StoryEvent`（情节事件），继承所有`Event`属性并可能包含额外的领域属性。

2.  **实体 (Entity)**
    *   **定义**: 代表在时间中持续存在的、可识别的客体（如人物、地点、物品），是图中的“名词”。实体的身份由其`uuid`恒定标识。
    *   **核心属性**:
        *   `uuid`: 全局唯一标识符。
        *   `entityType`: 实体类型，如 "Character"（人物）、"Location"（地点）、"Item"（物品）、"Organization"（组织）、"Skill"（技能）。
        *   `name`: 实体名称（必须为中文）。
        *   `createdAt`: 实体创建时间。
        *   `firstMentionChapter`: 首次出现的章节索引。
    *   **领域特化**: 实体通过双标签（如`:Entity:Character`）实现领域特化，从而继承领域特定的恒定属性。

3.  **状态 (State)**
    *   **定义**: 代表一个实体在特定时间点或时间段内的属性快照。状态是事件作用于实体的结果，实体的易变属性（如角色的境界、物品的持有者）都存储在状态节点中。
    *   **核心属性**:
        *   `uuid`: 状态唯一标识符。
        *   `valid_from_timestamp`: 状态生效的开始时间。
        *   `valid_to_timestamp`: 状态生效的结束时间（当前活跃状态为null）。
        *   `stateType`: 状态类型，如 "境界状态"、"技能状态"、"持有状态"等。
        *   `stateValue`: 状态的具体值（必须为中文）。

### 核心关系类型

节点之间的连接通过关系来定义，这些关系描述了节点间的语义关联。

*   **交互关系 (事件与实体/状态)**:
    *   `[:GENERATES]`: 事件生成了新实体。
    *   `[:TRANSFORMS]`: 事件转换了实体的状态。
    *   `[:TERMINATES]`: 事件终止了实体的生命周期。
    *   `[:OBSERVES]`: 事件观测了实体的状态但未改变。
    *   `[:REQUIRES_STATE]`: 事件的发生依赖于某个前置状态。
    *   `[:CREATES_STATE]`: 事件创建了新状态。

*   **状态演化关系 (状态与状态)**:
    *   `[:NEXT_STATE]`: 连接一个状态到其下一个版本，形成状态链。

*   **实体状态关系 (实体与状态)**:
    *   `[:CURRENT_STATE]`: 指向实体当前的活跃状态。
    *   `[:HAS_HISTORICAL_STATE]`: 指向实体已失效的历史状态。

*   **领域语义关系 (实体与实体)**:
    *   `[:FAMILY_OF]`, `[:FRIEND_OF]`, `[:ENEMY_OF]`等，用于描述角色间的社交关系。
    *   `[:BELONGS_TO]`, `[:LOCATED_IN]`等，用于描述从属关系。
    *   **重要原则**: 直接的行为关系（如攻击）和持有关系不被允许，必须通过`Event`节点中介，以保证因果链的完整性。

**Section sources**
- [kgKnowlage.md](file://file/kgKnowlage.md#L0-L933)
- [GraphService.java](file://ai/src/main/java/com/shuanglin/bot/service/GraphService.java#L30-L706)

## Cypher语句管理

`ArticlesEntity`类中的`cypherStatements`、`cypherExecuteStatus`、`cypherExecuteTime`等字段构成了一个完整的Cypher语句生命周期追踪系统，确保了知识图谱构建过程的可观测性和可追溯性。

*   **`cypherStatements`**: 该字段存储了由LLM根据`kgKnowlage.md`规范生成的原始Cypher语句。这些语句是将文本信息转化为图数据库操作的直接指令，是构建过程的核心产物。在`readStoryWithLimit`方法中，一旦LLM生成了Cypher，它会立即被更新到MongoDB中的`ArticlesEntity`记录里。

*   **`cypherExecuteStatus`**: 该字段用于追踪Cypher语句的执行状态，其值为`SUCCESS`、`FAILED`或`PENDING`。在`GraphService`执行Cypher之前，状态为`PENDING`；执行成功后，`ChapterStorageService`会调用`updateCypherExecuteStatus`方法将其更新为`SUCCESS`；若执行失败，则更新为`FAILED`。这为监控和调试提供了关键信息。

*   **`cypherExecuteTime`**: 该字段记录了Cypher语句执行完成的精确时间（ISO时间字符串），用于性能分析和审计。

*   **`cypherErrorMessage`**: 当执行失败时，详细的错误信息会被捕获并存储在此字段中，便于问题排查。

*   **`processStatus`**: 该字段反映了整个章节处理的宏观状态（`PENDING`/`PROCESSING`/`COMPLETED`/`FAILED`）。`ChapterStorageService`会根据`cypherExecuteStatus`的最终结果自动更新`processStatus`，例如，当Cypher执行成功时，`processStatus`会被设为`COMPLETED`。

这一套字段共同构成了一个从“生成”到“执行”再到“结果反馈”的闭环，使得整个知识图谱的构建过程透明且可控。

**Section sources**
- [ArticlesEntity.java](file://dbModel/src/main/java/com/shuanglin/dao/Articles/ArticlesEntity.java#L8-L55)
- [GraphService.java](file://ai/src/main/java/com/shuanglin/bot/service/GraphService.java#L329-L463)
- [ChapterStorageService.java](file://ai/src/main/java/com/shuanglin/bot/service/ChapterStorageService.java#L50-L97)

## 数据源与隔离

为了支持多本书籍或不同数据集的并行处理与管理，系统引入了`dataSource`和`bookUuid`字段来实现数据源的隔离与书籍级的知识组织。

*   **`dataSource`**: 这是一个字符串标识符（如`test_epub_40`），用于标记数据的来源。它被注入到Neo4j图数据库的每一个节点和关系中。通过`GraphService`提供的`cleanupTestData`和`queryTestDataStats`等方法，可以针对特定的`dataSource`执行数据清理或统计查询，从而实现不同数据集之间的完全隔离，避免数据污染。

*   **`bookUuid`**: 这是一个全局唯一的UUID，用于标识同一本书的所有章节。它是关联一本书籍所有`ArticlesEntity`记录的关键。`ChapterStorageService`提供了`queryChaptersByBook`等方法，允许根据`bookUuid`查询、统计或删除整本书的章节数据，实现了以书籍为单位的知识组织和管理。

这两个字段共同作用，确保了系统可以安全地处理多个独立的知识图谱项目。`IsolationMetadata`类封装了`dataSource`、`bookName`、`bookUuid`等元数据，并在`readStoryWithLimit`方法调用前进行严格验证，保证了数据隔离的正确性。

**Section sources**
- [ArticlesEntity.java](file://dbModel/src/main/java/com/shuanglin/dao/Articles/ArticlesEntity.java#L8-L55)
- [GraphService.java](file://ai/src/main/java/com/shuanglin/bot/service/GraphService.java#L329-L463)
- [IsolationMetadata.java](file://ai/src/main/java/com/shuanglin/bot/model/IsolationMetadata.java#L5-L146)

## 构建流程

从文本解析到知识图谱写入的完整流程是一个高度自动化的管道，其核心步骤如下：

1.  **文本解析与上下文准备**: `GraphService`使用`FileReadUtil`读取EPUB文件，将其解析为章节列表。对于每一章，它会聚合当前章节的文本，并获取上一章和下一章的文本作为上下文，用于实体一致性确认和消除歧义。

2.  **Prompt构建与LLM调用**: 使用`graphPromptTemplate`方法构建一个结构化的Prompt。该Prompt将`kgKnowlage.md`作为System Prompt（定义本体论和规则），并将当前章节的元数据（标题、索引、时间戳）和文本内容作为User Prompt。这个组合Prompt被发送给LLM（`DecomposeAssistant`）。

3.  **Cypher生成与验证**: LLM根据Prompt中的严格规范，从当前章节的文本中提取实体、事件和状态信息，并生成符合要求的Cypher语句。生成的Cypher会经过`validate`方法的多层校验，包括检查`paragraphIndex`属性、`source`格式以及变量引用规则，确保其符合本体论约束。

4.  **元数据注入与执行**: 通过`injectMetadata`方法，将`dataSource`、`bookUuid`等隔离元数据作为属性注入到Cypher语句中创建的每一个节点里。随后，`executeBatchCypher`方法在Neo4j事务中执行增强后的Cypher语句，确保操作的原子性。

5.  **状态追踪与持久化**: 在整个流程中，`ChapterStorageService`会持续更新`ArticlesEntity`在MongoDB中的状态。从保存原始文本，到更新`cypherStatements`，再到根据执行结果更新`cypherExecuteStatus`和`processStatus`，实现了对每个章节处理过程的完整追踪。

**Section sources**
- [GraphService.java](file://ai/src/main/java/com/shuanglin/bot/service/GraphService.java#L329-L463)
- [kgKnowlage.md](file://file/kgKnowlage.md#L0-L933)

## 查询模式

`GraphService`及其配套服务提供了多种基于图结构的智能查询模式，超越了简单的关键词搜索。

*   **`readStoryWithLimit`**: 这是最核心的查询模式，它不是一个简单的读取，而是一个“构建并查询”的复合操作。它根据`chapterLimit`限制处理的章节数，并通过`IsolationMetadata`指定数据源，实现了对特定书籍、特定范围的增量式知识图谱构建与查询。

*   **`replayCypherFromMongo`**: 该方法提供了一种故障恢复和重放的查询模式。当Neo4j数据丢失或需要重建时，可以从MongoDB中检索出已生成的Cypher语句，并重新执行到图数据库中，实现了知识图谱的可重放性。

*   **`queryTestDataStats`**: 该方法展示了跨存储系统的聚合查询能力。它同时查询MongoDB（获取章节处理状态）和Neo4j（获取节点数量统计），然后将结果合并，生成一个全面的数据统计报告，为系统监控提供了全局视图。

*   **`ChapterStorageService`中的查询方法**: 该服务提供了丰富的业务级查询，如`queryChapterByIndex`（按索引查询章节）、`queryFailedChapters`（查询失败的章节）、`getBookStatistics`（获取书籍统计信息）。这些查询利用了`bookUuid`和`chapterIndex`等字段，实现了以书籍为单位的精细化管理和分析。

这些查询模式共同构成了一个强大的知识检索和管理系统，支持从宏观统计到微观调试的多种需求。

**Section sources**
- [GraphService.java](file://ai/src/main/java/com/shuanglin/bot/service/GraphService.java#L329-L463)
- [ChapterStorageService.java](file://ai/src/main/java/com/shuanglin/bot/service/ChapterStorageService.java#L110-L246)

## 知识图谱示意图

下图展示了基于`kgKnowlage.md`中“萧炎”角色示例的简化知识图谱结构。它体现了核心的节点、关系和状态演化链。

```mermaid
graph TD
subgraph "核心节点"
E[Event:StoryEvent]
En[Entity:Character]
S[State]
end
subgraph "事件 (Event)"
e1(Event: 突破) --> |timestamp: 2025-01-05T00:00:00<br/>source: 第5章 突破| e1
e2(Event: 会面) --> |timestamp: 2025-01-03T00:00:00<br/>source: 第3章 会面| e2
end
subgraph "实体 (Entity)"
c1(Entity:Character) --> |name: 萧炎<br/>firstMentionChapter: 1| c1
c2(Entity:Character) --> |name: 纳兰嫣然| c2
end
subgraph "状态 (State)"
s1(State) --> |stateType: 境界状态<br/>stateValue: 斗之气三段<br/>valid_from: 2025-01-01<br/>valid_to: 2025-01-05| s1
s2(State) --> |stateType: 境界状态<br/>stateValue: 斗者<br/>valid_from: 2025-01-05<br/>valid_to: null| s2
end
e1 --> |[:TRANSFORMS]| c1
e1 --> |[:REQUIRES_STATE]| s1
e1 --> |[:CREATES_STATE]| s2
s1 --> |[:NEXT_STATE]| s2
c1 --> |[:CURRENT_STATE]| s2
c1 --> |[:HAS_HISTORICAL_STATE]| s1
e2 --> |[:PARTICIPATED_IN role:主角]| c1
e2 --> |[:PARTICIPATED_IN role:访客]| c2
e2 --> |[:OBSERVES]| c1
e2 --> |[:OBSERVES]| c2
style E fill:#f9f,stroke:#333
style En fill:#bbf,stroke:#333
style S fill:#f96,stroke:#333
```

**Diagram sources**
- [kgKnowlage.md](file://file/kgKnowlage.md#L0-L933)
- [GraphService.java](file://ai/src/main/java/com/shuanglin/bot/service/GraphService.java#L30-L706)

## 结论

本文档详细阐述了基于`GraphService`和`ArticlesEntity`的Neo4j知识图谱模型。该模型通过一个严谨的本体论框架，将非结构化的小说文本转化为结构化的图数据。其核心优势在于：
1.  **规范性**: 通过`kgKnowlage.md`和`validate`方法，确保了生成的图数据高度一致和规范。
2.  **可追溯性**: `ArticlesEntity`中的Cypher追踪字段提供了完整的构建过程审计能力。
3.  **可扩展性**: `dataSource`和`bookUuid`的设计支持多数据源的隔离与管理。
4.  **智能化**: 结合LLM的语义理解能力，实现了从文本到知识的自动化转换。

该模型为构建复杂、动态的知识图谱应用提供了一个坚实的基础，其设计原则和实现模式具有很高的参考价值。