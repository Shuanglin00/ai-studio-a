# **小说知识图谱增量构建规范 (v2.0 优化版)**

本文档定义了通过Cypher语句增量构建小说知识图谱的核心规范，专注于按章节时间轴组织实体状态，构建一个严谨、可扩展、并能高效支持后续检索和RAG应用的知识图谱结构。

---

### **第一章：元原则 (Core Principles)**

此为知识图谱增量构建的四个不可违背的基本公理。

1.  **因果性原则 (Principle of Causality):** 任何状态的改变都必须由一个`Event`（事件）显式或隐式地引起。`Entity`（实体）及其`State`（状态）是事件之间传递影响的唯一媒介。
2.  **章节顺序性原则 (Principle of Sequential Chapters):** 章节是绝对的、线性的、不可逆的时间标尺。所有图元素都在章节序列上拥有一个坐标 (`chapterIndex`)，且时间只向未来发展。
3.  **增量构建原则 (Principle of Incremental Construction):** 所有操作都是增量追加（Append-only）。新增章节内容时，只能创建新的节点和关系，**严禁修改或删除**已存在的节点属性或关系。
4.  **章节切片独立性原则 (Principle of Chapter Slice Independence):** 每个章节构成独立的时间切片。`State`节点记录实体在**当前章节的最终状态快照**，不包含历史变化信息，历史通过关系链追溯。

---

### **第二章：本体论框架 (Ontological Framework)**

此为构成知识图谱世界的基本元素，是元原则的具象化体现。

1.  **事件 (Event):**
    *   **定义:** 系统中状态变化的瞬时驱动者，是图中的**"动词"**。`Event`是因果性原则的执行者，**所有实体间的交互都必须通过`Event`节点来建模**。
2.  **实体 (Entity):**
    *   **定义:** 在故事中持续存在的、可识别的客体，是图中的**"名词"**。`Entity`是承载状态和传递因果的载体。
3.  **状态 (State):**
    *   **定义:** `Entity`在特定章节范围内的**属性快照**，是`Event`作用于`Entity`的结果。一个`State`节点代表实体在某个章节结束时的最终状态。

---

### **第三章：宇宙法则 (Rules of the Universe)**

这些法则是确保图谱世界一致性与逻辑严谨性的具体规则。

#### **3.1 实体交互与状态法则**

1.  **事件中心交互 (Event-centric Interactions):** 实体间的任何交互（如对话、战斗）都必须通过一个中心`Event`节点来表示。实体通过`:PARTICIPATES_IN`关系连接到事件，并通过关系属性（如`role`）来描述其在事件中的角色。
2.  **状态快照 (State Snapshots):** `State`记录实体在特定章节的**最终状态**。如果一个实体在一个章节内状态多次变化（如从“健康”到“受伤”再到“恢复”），`State`节点只记录其在本章结束时的最终状态“恢复”。过程由`Event`记录。
3.  **事件驱动状态 (Event-driven States):** `Event`是状态变化的原因。虽然模型中不强制要求每个`State`都链接到一个`Event`，但在逻辑上，任何`State`的产生都应能追溯到一个或多个`Event`。

#### **3.2 时间与历史法则**

1.  **不可变日志 (Immutable Log):** 整个图谱是一个基于章节的、只增不改的日志。任何"修正"或"更新"都必须通过在新的章节中创建新的节点和关系来体现。
2.  **状态版本链 (State Version Chains):** **此为核心规则。** 任何`Entity`的完整历史，都必须是一条由其历任`State`节点通过 `:NEXT_STATE` 关系按`chapterIndex`顺序连接而成的、**无间断的单向链条**。最新的`State`节点是链条的末端（没有出度的`:NEXT_STATE`关系）。
3.  **章节切片组织 (Chapter Slice Organization):** 引入`:Chapter`节点。每个章节通过 `HAS_CONTENT` 关系与该章节中创建的**所有**新节点（Event, State, 新Entity）关联，形成清晰的时间轴结构。

#### **3.3 边界与完整性法则**

1.  **外部事件 (Exogenous Events):** 允许存在没有内部因果前驱的`Event`，作为系统内部因果链的起点（例如故事开篇的背景设定）。
2.  **初始实体 (Initial Entities):** 系统可定义一组在`chapterIndex=1`时就存在的`Entity`及其初始`State`。
3.  **数据溯源 (Data Provenance):** 每个`Event`和`State`都必须附带`source`和`confidence`元数据，确保所有信息可追溯、可验证。

---

### **第四章：文本分析指导 (Text Analysis Guidelines)**

本章通过具体实例分析如何在小说文本中准确识别和区分实体、事件、状态，特别关注隐含事件的识别方法。

#### **4.1 实体识别原则**

*   **持续性:** 实体在故事中持续存在，具有跨章节的连续性。
*   **可识别性:** 实体有明确的名称、称号或特征描述。
*   **独立性:** 实体是独立存在的客体，可以参与交互。

**实例:** `"萧炎看着眼前的云岚宗..."` -> 识别实体: `萧炎 (Character)`, `云岚宗 (Organization)`。

#### **4.2 事件识别原则**

*   **显式事件:** 文本中有明确的动词描述，如"战斗"、"相遇"。
*   **隐式事件:** 需要通过上下文、对话、场景描述推断的事件。

**实例:** `"'你真的是云韵吗?' 云韵回答:'是的。'"` -> 识别隐式事件: `萧炎与云韵进行对话 (eventType: '对话')`。

#### **4.3 状态识别原则**

*   **时间性:** 状态属于特定章节，是该章节的快照。
*   **指向性:** 每个状态都明确指向其作用的实体。
*   **具体性:** 状态有明确的类型和值。

**实例:** `"萧炎的修为已经达到了斗师级别..."` -> 识别状态: `萧炎`的`境界状态`为`"斗师"`。

#### **4.4 状态与事件的区分**

*   **事件:** 描述"发生了什么"，是瞬时的、动作性的。
*   **状态:** 描述"是什么样子"，是持续的、属性性的。
*   **因果关系:** 事件引起状态变化。

**实例对比:**
*   事件: `"萧炎击败了对手"` (动作)
*   状态: `"萧炎是胜利者"` (属性)

#### **4.5 概念区分总结**

| 概念 | 核心问题 | 特征 | 文本表现 | 例子 |
| :--- | :--- | :--- | :--- | :--- |
| **实体 (Entity)** | "是谁/是什么？" | 持续性、可识别、独立客体 | 名词、代词、专有名词 | "萧炎", "云岚宗", "异火" |
| **事件 (Event)** | "发生了什么？" | 瞬时性、动作性、交互性 | 动词短语、场景描述 | "萧炎与纳兰嫣然定下三年之约" |
| **状态 (State)** | "处于什么样？" | 持续性、属性描述、章节快照 | 形容词、名词（表身份/等级） | "萧炎的境界是斗皇", "云岚宗处于备战状态" |

**核心关系：** `(实体)` --参与--> `[事件]` --引起--> `(新状态)`

---

### **第五章：Schema详尽定义 (Detailed Schema Definitions)**

本章对图谱中所有核心元素进行详尽说明，包括节点标签、属性、领域子类型以及关系类型。

#### **5.1 核心节点类型 (Core Node Types)**

##### **Chapter (章节)**
*   **节点标签:** `:Chapter`
*   **定义:** 代表小说的一个章节，是时间轴的基本单位和内容的容器。
*   **属性:**
    *   `chapterIndex` (Integer, **Unique**): 章节的唯一顺序编号。
    *   `name` (String): 章节标题。
    *   `summary` (String): 对本章内容的AI生成或人工编写的摘要。

##### **Event (事件)**
*   **节点标签:** `:Event`
*   **定义:** 在特定章节发生的、实体间交互行为的瞬时记录。
*   **属性:**
    *   `uuid` (String, **Unique**): 全局唯一标识符。
    *   `chapterIndex` (Integer): 事件发生的章节编号。
    *   `eventType` (String): 事件类型 (`对话`, `战斗`, `相遇`, `决策`, `修炼`等)。
    *   `description` (String): 对事件的自然语言描述。
    *   `sourceText` (String): **(推荐)** 触发事件的原文片段。
    *   `isImplicit` (Boolean): **(推荐)** 标记事件是文本明确描述的还是推理得出的。
    *   `confidence` (Float): 置信度。

##### **Entity (实体)**
*   **节点标签:** `:Entity`
*   **定义:** 在故事中持续存在的、可被识别的独立客体。这是所有领域实体（如角色、地点）的父标签。
*   **属性:**
    *   `uuid` (String, **Unique**): 实体跨越所有章节的恒定身份。
    *   `name` (String, **Indexed**): 实体的核心名称。
    *   `entityType` (String): 实体分类，其值对应下文的领域实体子类型（如 `Character`, `Location`）。
    *   `createdAtChapter` (Integer): 实体首次出现的章节索引。

##### **State (状态)**
*   **节点标签:** `:State`
*   **定义:** `Entity`在特定章节的**最终属性快照**。
*   **属性:**
    *   `uuid` (String, **Unique**): 状态版本的唯一标识符。
    *   `chapterIndex` (Integer): 此状态生效的章节。
    *   `stateType` (String): 状态的分类 (如 `境界`, `健康状况`, `地理位置`, `身份`, `持有物品`)。
    *   `stateValue` (String): 状态的具体值 (如 "斗皇", "重伤", "云岚宗", "宗主", "玄重尺")。
    *   `description` (String): 对状态的补充描述。

##### **Summary (摘要)**
*   **节点标签:** `:Summary`
*   **定义:** 跨越多个章节的、关于特定主题（如实体、关系、情节）的**综合性信息**，为RAG应用优化。
*   **属性:**
    *   `uuid` (String, **Unique**): 摘要的唯一标识符。
    *   `summaryType` (String): 摘要类型 (`人物小传`, `情节概述`, `关系演变`)。
    *   `content` (String): 摘要的详细文本内容。
    *   `startChapter` (Integer): 摘要涵盖的起始章节。
    *   `endChapter` (Integer): 摘要涵盖的结束章节。
    *   `source` (String): "AI Generated" 或 "Human Annotated"。

#### **5.2 领域实体子类型 (Domain Entity Subtypes)**

为了更精确地建模和查询，`:Entity`节点通过`entityType`属性和可选的附加标签进行细分。

*   **Character (角色)**
    *   **实现:** `entityType = 'Character'`，推荐使用复合标签 `:Entity:Character`。
    *   **特有属性:** `alias` (List<String>) - 别名、称号。
*   **Location (地点)**
    *   **实现:** `entityType = 'Location'`，推荐使用复合标签 `:Entity:Location`。
    *   **特有属性:** `locationType` (String) - 地点类型，如 `城市`, `宗门`, `山脉`, `秘境`。
*   **Organization (组织)**
    *   **实现:** `entityType = 'Organization'`，推荐使用复合标签 `:Entity:Organization`。
    *   **特有属性:** `orgType` (String) - 组织类型，如 `家族`, `帝国`, `佣兵团`。
*   **Item (物品)**
    *   **实现:** `entityType = 'Item'`，推荐使用复合标签 `:Entity:Item`。
    *   **特有属性:** `itemType` (String) - 物品类型，如 `武器`, `丹药`, `药材`。
*   **Skill (技能)**
    *   **实现:** `entityType = 'Skill'`，推荐使用复合标签 `:Entity:Skill`。
    *   **特有属性:** `skillType` (String) - 技能类型，如 `功法`, `斗技`, `身法`。

#### **5.3 关系类型 (Relationship Types)**

##### **结构与时序关系 (Structural & Temporal)**

*   `(Chapter)-[:HAS_CONTENT]->(Event|State|Entity)`: 章节与其包含的所有新内容的关联。
*   `(Entity)-[:HAS_STATE]->(State)`: 实体指向其一个状态快照。
*   `(State {chapterIndex: N})-[:NEXT_STATE]->(State {chapterIndex: M})` where `M > N`: **核心时序关系**，连接状态版本链，指向紧邻的后继状态。

##### **叙事与语义关系 (Narrative & Semantic)**

*   `(Entity)-[:PARTICIPATES_IN {role: String}]->(Event)`: 连接实体及其参与的事件。`role`属性描述其角色（如 '攻击方', '对话发起者', '被救者'）。
*   `(Event)-[:OCCURRED_IN]->(Location)`: 描述事件发生的地点实体。
*   `(Event)-[:TRIGGERED_BY]->(Event)`: 连接事件与其直接的前置因果事件。
*   `(Entity)-[:POSSIBLY_IDENTICAL]->(Entity)`: 实体消歧关系，表示两个实体节点可能指代同一现实客体（如“炎帝”和“萧炎”）。
*   `(Summary)-[:SUMMARIZES_ENTITY]->(Entity)`: 连接摘要及其描述的核心实体。
*   `(Summary)-[:COVERS_CHAPTERS]->(Chapter)`: 连接摘要及其涵盖的章节范围。

---

### **第六章：增量构建实现蓝图 (Incremental Construction Implementation Blueprint)**

#### **6.1 原子化Cypher增量构建模板**

**模板1: 初始化章节容器**
```cypher
// 确保章节节点存在，并设置其属性
MERGE (c:Chapter {chapterIndex: $chapterIndex})
ON CREATE SET c.name = $chapterName, c.summary = $chapterSummary
RETURN c
```

**模板2: 创建新实体及其初始状态**
```cypher
// 在特定章节创建全新的实体及其第一个状态节点
MATCH (c:Chapter {chapterIndex: $chapterIndex})
CREATE (e:Entity:$entityType { // 动态设置多标签
    uuid: randomUUID(),
    name: $entityName,
    entityType: $entityType,
    createdAtChapter: $chapterIndex
})
CREATE (s:State {
    uuid: randomUUID(),
    chapterIndex: $chapterIndex,
    stateType: $stateType,
    stateValue: $stateValue,
    description: $stateDescription
})
CREATE (e)-[:HAS_STATE]->(s)
CREATE (c)-[:HAS_CONTENT]->(e)
CREATE (c)-[:HAS_CONTENT]->(s)
RETURN e, s
```

**模板3: 更新实体状态 (核心模板优化)**
```cypher
// 关键操作：为现有实体创建新状态，并维护NEXT_STATE链
MATCH (c:Chapter {chapterIndex: $newChapterIndex})
MATCH (e:Entity {uuid: $entityUuid})
// 找到该实体当前最新的状态（没有出度 :NEXT_STATE 的状态）
OPTIONAL MATCH (e)-[:HAS_STATE]->(prevState:State)
WHERE NOT (prevState)-[:NEXT_STATE]->()

// 创建新状态
CREATE (newState:State {
    uuid: randomUUID(),
    chapterIndex: $newChapterIndex,
    stateType: $newStateType,
    stateValue: $newStateValue,
    description: $newStateDescription
})

// 建立关系
CREATE (e)-[:HAS_STATE]->(newState)
CREATE (c)-[:HAS_CONTENT]->(newState)

// 如果找到了前一个状态，则连接状态链
FOREACH (p IN CASE WHEN prevState IS NOT NULL THEN [prevState] ELSE [] END |
    CREATE (p)-[:NEXT_STATE]->(newState)
)

RETURN newState
```

**模板4: 创建实体交互事件**
```cypher
// 使用事件中心模型记录交互
MATCH (c:Chapter {chapterIndex: $chapterIndex})
MATCH (entityA:Entity {uuid: $entityA_uuid})
MATCH (entityB:Entity {uuid: $entityB_uuid})

CREATE (ev:Event {
    uuid: randomUUID(),
    chapterIndex: $chapterIndex,
    eventType: $eventType,
    description: $eventDescription,
    sourceText: $sourceText
})

CREATE (entityA)-[:PARTICIPATES_IN {role: $roleA}]->(ev)
CREATE (entityB)-[:PARTICIPATES_IN {role: $roleB}]->(ev)
CREATE (c)-[:HAS_CONTENT]->(ev)

// 如果事件发生在特定地点
WITH ev, c
OPTIONAL MATCH (loc:Location {uuid: $location_uuid})
MERGE (ev)-[:OCCURRED_IN]->(loc)

RETURN ev
```

**模板5: 创建摘要节点**
```cypher
// 创建一个总结性节点，并关联到实体和章节
MATCH (e:Entity {uuid: $entityUuid})
CREATE (s:Summary {
    uuid: randomUUID(),
    summaryType: '人物小传',
    content: $summaryContent,
    startChapter: $startChapter,
    endChapter: $endChapter,
    source: 'AI Generated'
})
CREATE (s)-[:SUMMARIZES_ENTITY]->(e)

// 关联所有相关章节
WITH s
MATCH (c:Chapter)
WHERE c.chapterIndex >= $startChapter AND c.chapterIndex <= $endChapter
CREATE (s)-[:COVERS_CHAPTERS]->(c)

RETURN s
```

#### **6.2 数据库级约束建议**

```cypher
// 核心UUID和索引唯一性
CREATE CONSTRAINT IF NOT EXISTS FOR (n:Entity) REQUIRE n.uuid IS UNIQUE;
CREATE CONSTRAINT IF NOT EXISTS FOR (n:Event) REQUIRE n.uuid IS UNIQUE;
CREATE CONSTRAINT IF NOT EXISTS FOR (n:State) REQUIRE n.uuid IS UNIQUE;
CREATE CONSTRAINT IF NOT EXISTS FOR (n:Summary) REQUIRE n.uuid IS UNIQUE;
CREATE CONSTRAINT IF NOT EXISTS FOR (n:Chapter) REQUIRE n.chapterIndex IS UNIQUE;

// 实体名称+类型联合唯一，防止同名同类实体重复
CREATE CONSTRAINT IF NOT EXISTS FOR (n:Entity) REQUIRE (n.name, n.entityType) IS UNIQUE;
```

---

### **第七章：增量构建操作流程 (Incremental Construction Workflow)**

#### **7.1 章节处理标准流程**

1.  **预处理 - 章节解析：**
    *   接收新章节文本。
    *   **实体识别与链接 (Entity Linking):** 识别文本中的实体提及，并将其链接到知识图谱中已存在的`Entity`节点的`uuid`。对于新实体，准备创建。这是最关键的前置步骤。
    *   **事件与状态提取：** 根据文本分析指导（第四章），提取事件和实体状态变化。
2.  **图构建 - 执行Cypher：**
    *   **步骤A - 章节容器:** 使用**模板1**创建或获取当前章节的`:Chapter`节点。
    *   **步骤B - 新实体:** 对于预处理中发现的新实体，使用**模板2**创建`Entity`及其初始`State`。
    *   **步骤C - 状态更新:** 对于已有实体的状态变化，使用**模板3**创建新的`State`节点并维护`:NEXT_STATE`链。
    *   **步骤D - 事件记录:** 使用**模板4**创建本章发生的`Event`节点，并关联参与的实体。
3.  **后处理 - 维护与增强：**
    *   **实体消歧:** 运行后台任务，根据新信息分析`:POSSIBLY_IDENTICAL`关系，进行实体合并。
    *   **摘要生成:** 定期（例如每10章）触发任务，使用**模板5**为核心实体或主要情节创建或更新`:Summary`节点，服务于RAG。

#### **7.2 增量构建约束检查**

*   **UUID唯一性:** 确保所有新建节点的uuid不重复。
*   **时间一致性:** 确保`chapterIndex`严格递增。
*   **状态指向:** 确保`State`节点正确地被其所属`Entity`引用。
*   **交互逻辑:** 确保参与`Event`的实体存在。
*   **增量原则:** 验证所有操作都是新增，不修改现有内容。
*   **状态链完整性:** 每次状态更新后，必须验证`Entity`的`:NEXT_STATE`链是否仍然是无分支、无环的单向链。这是数据质量的生命线。

#### **7.3 错误处理策略**

*   **重复实体检测:** 通过`POSSIBLY_IDENTICAL`关系处理可能的实体重复，交由离线任务或人工审核进行合并决策。
*   **状态冲突解决:** 本模型天然避免冲突。新状态不影响历史状态，通过`:NEXT_STATE`关系维护版本链。
*   **数据校验:** 在数据入库前进行Schema校验，确保所有必填字段存在且类型正确。
*   **事务性操作:** 将单个章节的所有Cypher语句包裹在同一个事务中执行，保证章节数据的原子性。若任一步骤失败，则回滚整个事务。