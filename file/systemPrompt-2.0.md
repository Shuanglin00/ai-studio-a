## **知识图谱宇宙：技术宪法与本体论全集 (Technical Constitution & Complete Ontology)**

本文档是构建小说知识图谱宇宙的最高纲领，内容涵盖从顶层哲学到底层实现的全部规范。

### **第一章：元原则 (Core Principles)**

此为本知识图谱宇宙运行的两个不可违背的基本公理。

1.  **因果性原则 (Principle of Causality):** 任何状态的改变都必须由一个`Event`（事件）引起。事件的发生可依赖于一个或多个实体处于特定前置状态。`Entity`（实体）及其`State`（状态）是事件之间传递影响的唯一媒介。
2.  **章节顺序性原则 (Principle of Sequential Chapters):** 章节是绝对的、线性的、不可逆的时间标尺。所有事件都在章节序列上拥有一个唯一的坐标 (`chapterIndex`)，且时间只向未来发展。

### **第二章：本体论框架 (Ontological Framework)**

此为构成知识图谱世界的基本元素，是元原则的具象化体现。

1.  **事件 (Event):**
    *   **定义:** 系统中状态变化的瞬时驱动者，是图中的**“动词”**。`Event`是因果性原则的执行者。
2.  **实体 (Entity):**
    *   **定义:** 在章节序列中持续存在的、可识别的客体，是图中的**“名词”**。`Entity`是承载状态和传递因果的载体。
3.  **状态 (State):**
    *   **定义:** `Entity`在特定章节范围内的属性快照，是`Event`作用于`Entity`的结果。

### **第三章：宇宙法则 (Rules of the Universe)**

这些法则是确保图谱世界一致性与逻辑严谨性的具体规则。

#### **3.1 因果与交互法则**

1.  **状态前置约束 (State-based Preconditions):** `Event`的发生必须严格依赖于一个或多个`Entity`处于特定的前置`State`。此法则通过 `(Event)-[:REQUIRES_STATE]->(State)` 关系实现。
2.  **四种基本交互 (Entity Interaction Types):** `Event`必须通过以下四种明确的方式与`Entity`交互：
    *   **生成 (Generation):** `Event`创造全新的`Entity`及其初始`State`。
    *   **终止 (Termination):** `Event`终结`Entity`的生命周期。
    *   **转换 (Transformation):** `Event`使`Entity`的某个`State`失效，并为其创建一个新的后继`State`。
    *   **观测 (Observation):** `Event`读取`Entity`的`State`但未改变它们。

#### **3.2 时间与历史法则**

1.  **不可变日志 (Immutable Log):** `Event`是不可变的记录。任何“修正”都必须通过创建一个新的、具有相反语义的`Event`来完成。
2.  **状态版本链 (State Version Chains):** 任何`Entity`的完整历史，都必须是一条由其历任`State`节点通过 `:NEXT_STATE` 关系按章节顺序连接而成的、无间断的链条。
3.  **系统“当前”的定义 (Definition of "Now"):** 系统的“当前状态”是所有`Entity`各自状态版本链中`valid_to_chapter`为`null`的末端`State`节点的集合。

#### **3.3 边界与完整性法则**

1.  **外部事件 (Exogenous Events):** 允许存在没有内部因果前驱的`Event`，作为系统内部因果链的起点。
2.  **初始实体 (Initial Entities):** 系统可定义一组在`chapterIndex=1`时就存在的`Entity`及其初始`State`。
3.  **数据溯源 (Data Provenance):** 每个`Event`和`State`都应附带`source`和`confidence`元数据。

### **第四章：Schema详尽定义 (Detailed Schema Definitions)**

本章以“定义-解释”的格式，对图谱中所有元素进行详尽说明。

#### **4.1 节点标签与属性 (Node Labels & Properties)**

##### **`:Event`**
*   **定义:** 一个在特定章节发生的、导致状态变化的**瞬时动作**。
*   **解释:** 图中的“动词”，因果性的驱动者。
*   **属性:**
    *   `uuid` (String): **定义:** 全局唯一标识符。**解释:** 事件的“身份证”。
    *   `chapterIndex` (Integer): **定义:** 事件发生的章节编号。**解释:** 事件在时间轴上的绝对坐标。
    *   `eventType` (String): **定义:** 四种基本交互类型之一 (`Generation`, `Transformation`, `Termination`, `Observation`)。**解释:** 明确事件的根本作用。
    *   `description` (String): **定义:** 对事件的自然语言业务描述。**解释:** 人类可读的事件摘要。
    *   `source` (String): **定义:** 格式为“第X章 章节名”的信息来源。**解释:** 保证数据可溯源。
    *   `confidence` (Float): **定义:** 0.0到1.0的置信度。**解释:** 量化信息提取的可靠性。

##### **`:Entity`**
*   **定义:** 一个在故事中持续存在的、可被识别的**独立客体**。
*   **解释:** 图中的“名词”，状态的承载者。
*   **属性:**
    *   `uuid` (String): **定义:** 全局唯一标识符。**解释:** 实体跨越所有章节的恒定身份。
    *   `name` (String): **定义:** 实体的核心名称。**解释:** 用于`MERGE`操作和人类识别。
    *   `entityType` (String): **定义:** 实体的领域分类 (如 `Character`, `Location`)。**解释:** 用于高层分类查询。
    *   `createdAt` (Integer): **定义:** 实体被创建的章节。
    *   `firstMentionChapter` (Integer): **定义:** 实体在原文中首次被提及的章节。
    *   `firstMentionSource` (String): **定义:** 首次提及的原文位置。

##### **`:State`**
*   **定义:** `Entity`在一段连续章节内的**属性快照**。
*   **解释:** 图中的“形容词”，描述实体在特定时间段内的样貌。
*   **属性:**
    *   `uuid` (String): **定义:** 全局唯一标识符。**解释:** 状态版本的“身份证”。
    *   `valid_from_chapter` (Integer): **定义:** 状态开始生效的章节。**解释:** 时间区间的起点。
    *   `valid_to_chapter` (Integer): **定义:** 状态失效的章节 (`null`表示当前有效)。**解释:** 时间区间的终点。
    *   `stateType` (String): **定义:** 状态的领域分类 (如 `境界状态`)。**解释:** 状态的业务分类。
    *   `stateValue` (String): **定义:** 该状态的核心业务值 (必须为中文)。**解释:** 如“斗皇”。

#### **4.2 领域实体子类型 (Domain Entity Subtypes)**
*   `:Entity:Character`: **定义:** 角色。**特有属性:** `alias` (List<String>) - 别名、称号。
*   `:Entity:Location`: **定义:** 地点。**特有属性:** `locationType` (String) - 城市、宗门等。
*   `:Entity:Organization`: **定义:** 组织。**特有属性:** `orgType` (String) - 家族、帝国等。
*   `:Entity:Item`: **定义:** 物品。**特有属性:** `itemType` (String) - 武器、丹药等。
*   `:Entity:Skill`: **定义:** 技能。**特有属性:** `skillType` (String) - 功法、斗技等。

#### **4.3 关系类型 (Relationship Types)**

*   **因果关系 (Causal):**
    *   `Event -[:GENERATES|TERMINATES|TRANSFORMS|OBSERVES]-> Entity`
    *   `Event -[:CREATES_STATE]-> State`
    *   `Event -[:REQUIRES_STATE]-> State`: **定义:** 事件发生依赖的前置状态。
*   **时序关系 (Temporal):**
    *   `Entity -[:CURRENT_STATE]-> State`: **定义:** 指向当前活跃状态。
    *   `Entity -[:HAS_HISTORICAL_STATE]-> State`: **定义:** 指向已失效的历史状态。
    *   `State -[:NEXT_STATE]-> State`: **定义:** 连接状态版本链，指向后继状态。
*   **叙事关系 (Narrative):**
    *   `(Entity)-[:PARTICIPATED_IN]->(Event)`: **定义:** 连接实体及其参与的事件。**解释:** 这是构建**事件枢纽模型**的核心，其属性（如 `role`, `motivation`, `emotion`, `quote`）用于捕捉瞬时描述。
    *   `(Event)-[:OCCURRED_AT]->(Location)`: **定义:** 描述事件发生的地点。
    *   `(Event)-[:FEATURED_ITEM|:FEATURED_SKILL]->(Entity)`: **定义:** 描述事件中的关键物品或技能。
    *   `(Event)-[:TRIGGERED_BY]->(Event)`: **定义:** 连接事件与其直接的前置因果事件。

### **第五章：实现蓝图 (Implementation Blueprint)**

#### **5.1 叙事细节建模规范**

1.  **瞬时描述 (通过关系属性):** 仅在事件期间有效的描述（表情、对话、动作），作为**:PARTICIPATED_IN**关系的属性。
2.  **持续性状态描述 (通过State节点属性):** 实体在一段时间内的持续状态（地点氛围、角色心境），作为**:State**节点的附加属性 (如 `atmosphere`)。
3.  **内在固有描述 (通过Entity节点属性):** 实体的永久性特征（物品材质、先天体质），作为**:Entity**节点的属性 (如 `material`)。

#### **5.2 原子化Cypher操作模板**

**模板1: 生成新实体及初始状态**
```cypher
// 遵循：因果性原则、生成(Generation)交互法则
CREATE (e:Event:StoryEvent {
  uuid: randomUUID(),
  chapterIndex: 1,
  eventType: 'Generation',
  source: '第1章 落魄天才',
  confidence: 1.0,
  description: '创建论文实体'
})
CREATE (entity:Entity:Paper {
  uuid: randomUUID(),
  entityType: 'Paper',
  createdAt: 1,
  name: '知识图谱研究',
  firstMentionChapter: 1,
  firstMentionSource: '第1章 落魄天才'
})
CREATE (s:State {
  uuid: randomUUID(),
  valid_from_chapter: 1,
  valid_to_chapter: null,
  stateType: 'status',
  stateValue: 'draft'
})
CREATE (e)-[:GENERATES {chapterIndex: 1}]->(entity)
CREATE (e)-[:CREATES_STATE {chapterIndex: 1}]->(s)
CREATE (entity)-[:CURRENT_STATE]->(s)
```

**模板2: 转换实体状态 (原子化事务)**
```cypher
// 遵循：因果性原则、转换(Transformation)交互、状态前置约束、状态版本链法则
MATCH (entity:Entity {uuid: $entity_uuid})-[rel:CURRENT_STATE]->(currentState:State)
WHERE currentState.stateValue = 'draft' // 严格的前置条件检查
CREATE (e:Event {
  uuid: randomUUID(),
  chapterIndex: $event_chapterIndex,
  eventType: 'Transformation',
  source: $source,
  confidence: $confidence,
  description: '论文提交审核'
})
CREATE (newState:State)
SET newState = $new_status_properties, // e.g., {stateValue: 'approved'}
    newState.uuid = randomUUID(),
    newState.valid_from_chapter = $event_chapterIndex,
    newState.valid_to_chapter = null
SET currentState.valid_to_chapter = $event_chapterIndex
DELETE rel
CREATE (e)-[:TRANSFORMS]->(entity)
CREATE (e)-[:REQUIRES_STATE {required_condition: 'stateValue=draft'}]->(currentState)
CREATE (e)-[:CREATES_STATE]->(newState)
CREATE (currentState)-[:NEXT_STATE {transition_event_uuid: e.uuid}]->(newState)
CREATE (entity)-[:CURRENT_STATE]->(newState)
CREATE (entity)-[:HAS_HISTORICAL_STATE]->(currentState)
```

#### **5.3 数据库级约束建议**

```cypher
// 保证核心元素的UUID唯一
CREATE CONSTRAINT entity_uuid_unique IF NOT EXISTS FOR (n:Entity) REQUIRE n.uuid IS UNIQUE;
CREATE CONSTRAINT event_uuid_unique IF NOT EXISTS FOR (n:Event) REQUIRE n.uuid IS UNIQUE;
CREATE CONSTRAINT state_uuid_unique IF NOT EXISTS FOR (n:State) REQUIRE n.uuid IS UNIQUE;

// 保证领域实体的name在其类型内部唯一
CREATE CONSTRAINT character_name_unique IF NOT EXISTS FOR (n:Character) REQUIRE n.name IS UNIQUE;
CREATE CONSTRAINT location_name_unique IF NOT EXISTS FOR (n:Location) REQUIRE n.name IS UNIQUE;
CREATE CONSTRAINT organization_name_unique IF NOT EXISTS FOR (n:Organization) REQUIRE n.name IS UNIQUE;
```