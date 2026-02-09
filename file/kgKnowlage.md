### **一、 元原则 (Core Principles)**

这是本知识图谱宇宙运行的两个不可违背的基本公理。

1. **因果性原则 (Principle of Causality):** 任何状态的改变都必须由一个事件引起。事件的发生可能依赖于一个或多个前置条件（特定实体处于特定状态）。实体（或其状态）是事件之间传递影响的媒介。
2. **章节顺序性原则 (Principle of Sequential Chapters):** 章节是绝对的、线性的、不可逆的。所有事件都在章节序列上拥有一个唯一的坐标，且章节只向后续章节发展。

### **二、 本体论框架 (Ontological Framework)**

这是构成知识图谱世界的基本元素。

1. **事件 (Event):** **定义:** 系统中状态变化的瞬时驱动者，是图中的"动词"。一个事件本身不具有持续时间，它发生在特定的章节中。**核心属性:**uuid: 全局唯一标识符。chapterIndex: Integer 类型，事件发生的章节索引，一旦记录不可修改。
2. **实体 (Entity):** **定义:** 一个在章节序列中持续存在的、可识别的客体（物理或概念上的），是图中的"名词"。实体本身是其所有历史状态的集合，其uuid代表了其跨越章节的唯一身份。**核心属性:**uuid: 全局唯一标识符，代表其恒定的身份。
3. **状态 (State):** **定义:** 一个实体在特定章节或章节范围内的属性快照。状态是事件作用于实体的结果。实体本身不直接存储易变属性，而是通过其拥有的状态来体现。**核心属性:**valid_from_chapter: 此状态的起始生效章节，通常由创生此状态的事件章节决定。valid_to_chapter: 此状态的结束生效章节，由下一个改变实体的事件决定。以及其他描述该实体在该章节属性的键值对（例如 status: "approved", value: 100）。

### **三、 因果与约束规则 (Causal and Dependency Rules)**

这是对"约束与继承"原则的具象化。

1. **状态前置约束 (State-based Preconditions):** 一个事件能否发生，其约束条件被精确定义为：**一个或多个实体必须处于特定的前置状态**。这超越了简单的"事件依赖事件"，而是基于更严格的"状态依赖"。*示例:* "论文发表"事件的发生，其前置条件是"论文"实体的状态为{status: "peer_reviewed_approved"}。
2. **实体交互类型 (Entity Interaction Types):** 事件通过以下四种基本方式与实体及其状态进行交互，从而实现"继承"与演化。**生成 (Generation):** 事件创造了一个全新的实体及其初始状态。这是因果链中新物质的来源。**终止 (Termination):** 事件将一个实体的最终状态标记为"终结"，使其不再参与后续的演化。这不意味着删除数据，而是逻辑上的生命周期结束。**转换 (Transformation):** 事件导致一个实体从一个状态迁移到一个新的状态。这是最常见的交互，具体表现为使旧状态失效（赋予valid_to_chapter），并创建一个链接到该实体的新状态。**观测 (Observation):** 事件读取了一个或多个实体的状态，但并未改变它们。这用于建立非侵入性的依赖关系，例如一个"决策"事件依赖于对多个"市场报告"实体状态的观测。

### **四、 章节规则 (Chapter Rules)**

这是对"章节线性发展"原则的具象化。

1. **不可变日志 (Immutable Log):** 整个知识图谱本质上是一个关于事件的不可变日志。不允许删除或修改任何已发生的事件。任何"修正"或"撤销"操作，都必须通过在当前章节创建一个新的、具有相反语义的事件来完成。
2. **状态版本链 (State Version Chains):** 任何实体的完整历史都是一条由其历任状态节点按章节顺序连接而成的链。这使得可以查询到：任意实体的当前状态（状态链的末端节点）。任意实体在过去任意章节的历史状态。一个实体从诞生到现在的完整演变路径。
3. **系统"当前状态"的定义 (Definition of System "Now"):** 系统的"当前"并非一个特殊的标记，而是一个动态计算的结果。它是整个图中所有实体各自状态版本链末端节点的集合。

### **五、 系统边界与完整性规则 (System Boundary and Integrity Rules)**

这些规则定义了系统的边界、如何处理外部信息以及如何保证数据质量。

1. **外部事件 (Exogenous Events):** 允许存在没有内部因果前驱的事件。这类事件代表了从被建模系统外部接收到的输入或刺激。它们的因果源头被认定为"系统外部"，而非"不存在"。它们是系统内因果链的起点。
2. **初始实体 (Initial Entities):** 系统可以定义一组在"第一章"（chapterIndex=1）就已经存在的实体及其初始状态。它们是系统开始演化时的公理化起点，无需"生成"事件。
3. **数据源与置信度 (Data Provenance and Confidence):** 为确保知识图谱的严谨性,每个事件和状态都应附带元数据:**来源 (Source):** 记录该信息的来源系统、传感器或人工录入者。**置信度 (Confidence):** 一个量化指标(如0-1之间的小数),表示系统对该条信息真实性的评估。置信度可以在因果链中根据预设规则进行传播或衰减。例如,经过多次"转换"的实体状态,其置信度可能会低于其原始状态。

#### **6.1 节点标签定义**

1. **Event节点:** 使用标签 `:Event`,必须包含属性:
   - `uuid`: String - 全局唯一标识符
   - `chapterIndex`: Integer - 章节索引(从1开始)
   - `eventType`: String - 事件类型(如 "Generation", "Transformation", "Termination", "Observation")
   - `source`: String - 数据来源(格式: "第X章 章节名")
   - `confidence`: Float - 置信度(0.0-1.0)
   - `description`: String - 事件业务描述

2. **Entity节点:** 使用标签 `:Entity`,必须包含属性:
   - `uuid`: String - 全局唯一标识符
   - `entityType`: String - 实体类型(如 "Character", "Location", "Item", "Skill", "Organization")
   - `createdAt`: Integer - 实体创建章节
   - `name`: String - 实体名称(必须为中文)
   - `firstMentionChapter`: Integer - 首次出现章节
   - `firstMentionSource`: String - 首次出现位置

3. **State节点:** 使用标签 `:State`,必须包含属性:
   - `uuid`: String - 状态唯一标识符
   - `valid_from_chapter`: Integer - 状态生效开始章节(必须等于创建它的Event.chapterIndex)
   - `valid_to_chapter`: Integer - 状态生效结束章节(当前活跃状态必须为null)
   - `stateType`: String - 状态类型(如 "境界状态", "技能状态", "关系状态", "地理状态", "持有状态")
   - `stateValue`: String - 状态具体值(必须为中文)
   - 其他业务属性(根据stateType定义)

#### **6.2 关系类型定义**

1. **GENERATES:** Event -[:GENERATES]-> Entity
   - 表示事件生成了新实体
   - 属性: `chapterIndex`: Integer

2. **TERMINATES:** Event -[:TERMINATES]-> Entity
   - 表示事件终止了实体的生命周期
   - 属性: `chapterIndex`: Integer

3. **TRANSFORMS:** Event -[:TRANSFORMS]-> Entity
   - 表示事件转换了实体的状态
   - 属性: `chapterIndex`: Integer

4. **OBSERVES:** Event -[:OBSERVES]-> Entity
   - 表示事件观测了实体状态但未改变
   - 属性: `chapterIndex`: Integer

5. **CURRENT_STATE:** Entity -[:CURRENT_STATE]-> State
   - 描述：表示这是该实体的当前活跃状态。一个实体在任一时刻只能有一个:CURRENT_STATE关系。

6. **HAS_HISTORICAL_STATE:** Entity -[:HAS_HISTORICAL_STATE]-> State
   - 描述：表示这是该实体的一个已失效的历史状态。

7. **REQUIRES_STATE:** Event -[:REQUIRES_STATE]-> State
   - 描述：表示事件的前置状态依赖。
   - 属性: `required_condition`: String - 具体的状态约束条件描述

8. **CREATES_STATE:** Event -[:CREATES_STATE]-> State
   - 描述：表示事件创建了新状态。
   - 属性: `chapterIndex`: Integer

9. **NEXT_STATE:** State -[:NEXT_STATE]-> State
   - 描述：连接状态版本链，指向该状态的下一个版本。
   - 属性: `transition_event_uuid`: String - 触发转换的事件UUID

#### **6.3 Cypher语句生成提示词**

**提示词用途说明:**

本提示词用于指导AI系统根据知识图谱文档的内容,自动生成符合规范的Cypher语句。具体用途包括:

1. **章节内容理解与转换:** AI需要阅读前一章(元原则、本体论框架)，当前章节和一下章节的内容描述,理解其中蕴含的事件、实体和状态关系,然后将这些概念性描述转换为可执行的Cypher语句。

2. **自动化建模:** 当用户提供新的章节内容或业务场景描述时,AI应:
   - 识别描述中的核心实体(如"人物"、"技能"、"道具"等)
   - 提取事件及其类型(生成、转换、终止、观测)
   - 分析状态变化和前置条件依赖
   - 自动生成完整的节点创建和关系建立语句

3. **保证一致性:** 确保所有生成的Cypher语句都严格遵循:
   - 本文的第一章定义的因果性原则和时间单向性原则
   - 本文的第二章定义的本体论框架(Event、Entity、State的结构)
   - 本文的第三章定义的因果与约束规则
   - 本文的第四章定义的时间规则
   - 本文的第五章定义的系统边界与完整性规则

**系统角色定义:**
```
你是一个专业的知识图谱Cypher语句生成助手。你的任务是:

1. 理解输入内容:仔细阅读用户提供的章节内容或场景描述,识别其中的实体、事件和状态信息。

2. 映射到本体框架:将描述内容映射到本文档第一章和第二章定义的本体论框架:
   - 识别哪些是实体(Entity)及其类型
   - 识别哪些是事件(Event)及其交互类型(Generation/Transformation/Termination/Observation)
   - 识别状态变化和前置条件依赖
   - 自动生成完整的节点创建和关系建立语句

3. 生成规范Cypher:严格按照第六章定义的节点标签、关系类型和生成模板,输出完整的Cypher语句。

4. 验证与注释:为生成的每条语句添加清晰的注释,说明其对应的业务语义,并确保符合所有验证规则。
```

**核心要求:**

1. **严格遵守因果性原则:**
   - 每个状态变化必须由一个Event节点触发
   - 必须明确标记事件与实体的交互类型(GENERATES/TRANSFORMS/TERMINATES/OBSERVES)

2. **严格遵守章节顺序性原则:**
   - 所有chapterIndex必须使用Integer类型
   - Event的chapterIndex一旦设置不可修改
   - State的valid_from_chapter和valid_to_chapter必须形成合法的章节区间
   - **小说场景章节索引生成策略:**
     * 章节索引按顺序递增：第1章、第2章、第3章...
     * 示例: 第5章 → chapterIndex: 5

3. **维护状态版本链:**
   - 当创建新状态时,必须:
     a. 将旧状态的valid_to_chapter设置为当前事件的chapterIndex
     b. 创建新状态节点,其valid_from_chapter为当前事件的chapterIndex
     c. 创建NEXT_STATE关系连接旧状态到新状态

4. **处理前置条件:**
   - 如果事件有状态依赖,必须创建REQUIRES_STATE关系
   - 需验证前置状态在事件章节是否有效(valid_from <= event.chapterIndex < valid_to)

**生成模板示例:**

**场景1: 创建新实体及初始状态**
```cypher
// 1. 创建生成事件
CREATE (e:Event {
  uuid: randomUUID(),
  chapterIndex: 1,
  eventType: 'Generation',
  source: '第1章 落魄天才',
  confidence: 1.0,
  description: '创建论文实体'
})

// 2. 创建实体
CREATE (entity:Entity {
  uuid: randomUUID(),
  entityType: 'Paper',
  createdAt: 1,
  name: '知识图谱研究',
  firstMentionChapter: 1,
  firstMentionSource: '第1章 落魄天才'
})

// 3. 创建初始状态
CREATE (s:State {
  uuid: randomUUID(),
  valid_from_chapter: 1,
  valid_to_chapter: null,
  stateType: 'status',
  stateValue: 'draft',
  version: 1
})

// 4. 建立关系
CREATE (e)-[:GENERATES {chapterIndex: 1}]->(entity)
CREATE (e)-[:CREATES_STATE {chapterIndex: 1}]->(s)
// 使用 CURRENT_STATE 关系来表示这是实体的当前活跃状态
CREATE (entity)-[:CURRENT_STATE]->(s)
```

// 场景2: 原子化的状态转换 (含前置条件检查)
// 外部传入参数: $entity_uuid, $event_chapterIndex, $source, $confidence, $new_status_properties (一个包含新状态所有业务属性的Map)

// 1. 查找满足前置条件的实体和它的当前状态
```cypher
MATCH (entity:Entity {uuid: $entity_uuid})-[rel:CURRENT_STATE]->(currentState:State)
WHERE currentState.status = 'draft' // 严格的前置条件检查

WITH entity, rel, currentState,
     // 2. 在WITH子句中准备好所有新节点和属性，确保事务性
     {
       uuid: randomUUID(),
       chapterIndex: $event_chapterIndex,
       eventType: 'Transformation',
       source: $source,
       confidence: $confidence,
       description: '论文提交审核'
     } AS eventProps,
     {
       uuid: randomUUID(),
       valid_from_chapter: $event_chapterIndex,
       valid_to_chapter: null
     } + $new_status_properties AS newStateProps // 将外部传入的业务属性合并进来

// 3. 创建新节点
CREATE (e:Event) SET e = eventProps
CREATE (newState:State) SET newState = newStateProps

// 4. 创建新关系
CREATE (e)-[:TRANSFORMS]->(entity)
CREATE (e)-[:REQUIRES_STATE {required_condition: 'status=draft'}]->(currentState)
CREATE (e)-[:CREATES_STATE]->(newState)
CREATE (currentState)-[:NEXT_STATE {transition_event_uuid: e.uuid}]->(newState)
CREATE (entity)-[:CURRENT_STATE]->(newState) // 创建指向新状态的CURRENT_STATE关系
CREATE (entity)-[:HAS_HISTORICAL_STATE]->(currentState) // 将旧状态标记为历史

// 5. 解除旧关系并更新旧状态
DELETE rel // 删除旧的CURRENT_STATE关系
SET currentState.valid_to_chapter = $event_chapterIndex

RETURN e.uuid AS eventId, newState.uuid AS newStateId
```
**场景3: 查询实体历史状态**
```cypher
// 查询实体在特定章节的状态
MATCH (entity:Entity {uuid: $entity_uuid})-[:HAS_STATE]->(s:State)
WHERE s.valid_from_chapter <= $query_chapter
  AND (s.valid_to_chapter IS NULL OR s.valid_to_chapter > $query_chapter)
RETURN s

// 查询实体的完整状态演化链
MATCH (entity:Entity {uuid: $entity_uuid})-[:HAS_STATE]->(s:State)
OPTIONAL MATCH path = (s)-[:NEXT_STATE*]->()
RETURN path
ORDER BY s.valid_from_chapter
```

**场景4: 查询因果链**
```cypher
// 追溯某个状态的因果来源
MATCH (s:State {uuid: $state_uuid})<-[:CREATES_STATE]-(e:Event)
OPTIONAL MATCH (e)-[:REQUIRES_STATE]->(prereqState:State)
OPTIONAL MATCH (prereqState)<-[:CREATES_STATE]-(prereqEvent:Event)
RETURN e, prereqState, prereqEvent

// 查找事件的所有直接影响
MATCH (e:Event {uuid: $event_uuid})
OPTIONAL MATCH (e)-[r:GENERATES|TRANSFORMS|TERMINATES|OBSERVES]->(affected:Entity)
OPTIONAL MATCH (e)-[:CREATES_STATE]->(newState:State)
RETURN e, r, affected, newState
```

#### **6.4 生成约束与验证规则**

在生成Cypher语句前,必须验证:

**通用验证规则:**
1. **UUID唯一性:** 所有新创建的节点必须生成唯一UUID
2. **章节一致性:** Event.chapterIndex <= State.valid_from_chapter
3. **状态链完整性:** 同一实体的状态不能有章节重叠
4. **前置条件满足:** 执行TRANSFORM/TERMINATE前必须验证前置状态存在且有效
5. **关系完整性:** 每个State必须关联到至少一个Entity和一个创建它的Event

**小说场景专用验证规则:**
6. **Event属性完整性:** Event节点必须包含chapterIndex、description、source属性
7. **Entity属性完整性:** Entity节点必须包含name、firstMentionChapter、firstMentionSource属性
8. **State属性规范:** State节点必须包含stateType和stateValue属性（中文值）
9. **章节索引验证:** chapterIndex必须使用正整数
10. **信息来源验证:** 所有新节点必须来自indexText，不能来自lastContext或nextContext
11. **空输出验证:** 如果indexText无新信息，必须返回空字符串而非文字说明
12. **属性命名规范:** 所有属性键名和值（业务相关）必须使用中文

---

# 6.5 领域本体设计规范 (Domain Ontology Design Specification)

本章定义了在通用本体论框架（第二至四章）基础上，构建小说知识图谱所需的领域特定实体与关系模型。其核心目标是**以结构化的方式，无损地浓缩并表示小说原文中的叙事信息**。

## 指导哲学 (Guiding Philosophy)

1.  **万物皆实体 (Everything is an Entity):** 人物、地点、物品、技能、组织等叙事元素，皆为图中的`:Entity`节点。
2.  **万事皆事件 (Everything Happens in an Event):** 实体间的所有动态交互（战斗、对话、结盟）都必须通过一个中介的`:Event`节点来描述。事件是连接所有叙事碎片的上下文枢纽。
3.  **状态时序化 (States are Temporal):** 实体的可变属性（如境界、位置、持有者）不直接存储在实体上，而是封装在有时序性的`:State`节点中，以精确回溯任一时间点的世界状态。

---

## 6.5.1 核心语法与命名约定 (Core Syntax & Naming Conventions)

#### A. 节点标签 (Node Labels)

-   **继承原则:** 领域实体必须使用多标签继承，基础标签在前，特化标签在后。
    -   **推荐:** `(c:Entity:Character)`，`(e:Event:StoryEvent:Combat)`
-   **语法:** 标签使用冒号`:`分隔，无空格。
    -   **规范:** `(n:Label1:Label2)`

#### B. 关系类型 (Relationship Types)

-   **单一类型原则:** 一个关系只能有一个类型。关系的多样性通过其属性来表达。
    -   **推荐:** `(c1)-[:PARTICIPATED_IN {role: "attacker"}]->(e)`
-   **命名:** 关系类型使用大写字母和下划线（SNAKE\_CASE），如 `PARTICIPATED_IN`, `CURRENT_STATE`。

#### C. 属性命名 (Property Naming)

-   **规范:** 所有属性键（key）统一使用驼峰命名法（camelCase），如 `entityType`, `firstMentionChapter`。
-   **数据类型:** 明确定义属性值的数据类型，如 `String`, `Integer`, `Float`, `List<String>`, `Boolean`。

---

## 6.5.2 领域实体类型定义 (Domain Entity Types)

所有实体共享以下**基础属性**:

-   `uuid: String` (Unique, Indexed) - 全局唯一标识符
-   `name: String` (Indexed) - 实体的核心名称，用于MERGE操作
-   `entityType: String` - 实体类型枚举值（"Character", "Location"等）
-   `createdAtChapter: Integer` - 实体在图中被创建时对应的章节
-   `firstMentionChapter: Integer` - 实体在原文中首次被提及的章节
-   `sourceText: String` - 首次提及的原文片段，用于溯源

#### 1. Character (角色)

-   **标签:** `:Entity:Character`
-   **恒定属性:** `alias: List<String>` - 别名、称号、化名列表

#### 2. Location (地点)

-   **标签:** `:Entity:Location`
-   **恒定属性:** `locationType: String` - 地点类型（如 "城市", "山脉", "宗门", "秘境"）

#### 3. Organization (组织)

-   **标签:** `:Entity:Organization`
-   **恒定属性:** `orgType: String` - 组织类型（"家族", "宗门", "帝国", "商会"）

#### 4. Item (物品)

-   **标签:** `:Entity:Item`
-   **恒定属性:**
    -   `itemType: String` - 物品类型（"武器", "丹药", "功法卷轴", "天材地宝"）
    -   `material: String` - 固有材质（可选）
    -   `description: String` - 物品的固有描述（可选）

#### 5. Skill (技能)

-   **标签:** `:Entity:Skill`
-   **恒定属性:**
    -   `skillType: String` - 技能类型（"功法", "斗技", "身法"）
    -   `grade: String` - 品阶（"天阶", "地阶", "玄阶", "黄阶"）

---

## 6.5.3 动态状态节点定义 (Dynamic State Nodes)

状态节点用于封装实体的时变属性。

-   **基础标签:** `:State`
-   **基础属性:**
    -   `uuid: String` (Unique)
    -   `validFromChapter: Integer` - 状态生效的起始章节
    -   `validToChapter: Integer` - 状态失效的章节（`null`表示当前有效）
    -   `sourceText: String` - 描述该状态的原文片段

#### 状态子类型 (State Subtypes):

-   **标签:** `:State:CultivationState` (境界状态)
    -   `level: String` - 境界等级 (如 "斗者")
    -   `description: String` - 状态描述 (如 "根基不稳", "气息雄浑")
-   **标签:** `:State:PossessionState` (持有状态)
    -   `owner: String` - 持有者（Character的`name`）
    -   `quantity: Integer` - 持有数量
-   **标签:** `:State:LocationState` (地理状态)
    -   `controller: String` - 控制者（Character或Organization的`name`）
    -   `status: String` - 地点当前状况（"繁荣", "战乱"）
    -   `atmosphere: String` - 环境氛围描述
-   **标签:** `:State:RelationshipState` (关系状态)
    -   `target: String` - 关系指向对象（Character的`name`）
    -   `relation: String` - 关系类型（"盟友", "敌人", "师徒"）
    -   `level: Integer` - 关系深度（-10到10）

---

## 6.5.4 事件与关系建模 (Event and Relationship Modeling)

#### A. StoryEvent (叙事事件)

-   **标签:** `:Event:StoryEvent` (可追加如 `:Combat`, `:Dialogue` 等子类型)
-   **核心属性:**
    -   `uuid: String` (Unique)
    -   `chapterIndex: Integer` (Indexed)
    -   `eventType: String` - 本体论事件类型 (`Generation`, `Transformation`, `Observation`, `Termination`)
    -   `summary: String` - AI生成的事件一句话摘要
    -   `sourceText: String` - 触发事件的核心原文
    -   `confidence: Float` - 提取置信度 (0.0 - 1.0)

#### B. 关系建模指南 (Relationship Modeling Guide)

**原则：用事件连接实体，用关系描述参与方式。**

1.  **结构性关系 (Structural Relationships):** 定义世界观的静态结构。
    -   `[:CONTAINS]` - (父地点)-[:CONTAINS]->(子地点)
    -   `[:PART_OF]` - (组织)-[:PART_OF]->(上级组织)
2.  **叙事性关系 (Narrative Relationships):** 描述故事中的动态交互，**必须通过Event节点中介**。
    -   `[:PARTICIPATED_IN]` - (实体)-[:PARTICIPATED\_IN]->(事件)
    -   `[:OCCURRED_AT]` - (事件)-[:OCCURRED\_AT]->(地点)
    -   `[:FEATURED_ITEM]` - (事件)-[:FEATURED\_ITEM]->(物品)
    -   `[:FEATURED_SKILL]` - (事件)-[:FEATURED\_SKILL]->(技能)
    -   `[:CREATES_STATE]` - (事件)-[:CREATES\_STATE]->(状态)
    -   `[:TRIGGERED_BY]` - (当前事件)-[:TRIGGERED\_BY]->(前置事件)

---

## 6.5.5 黄金模板：使用事件枢纽（Event Hub）模式

此模板是本规范的核心，用于对小说中任何动态场景进行建模。

```cypher
// 场景: "三年之约，萧炎在云岚宗击败纳兰嫣然"

// 步骤 1: MERGE所有参与的实体 (确保存在性)
MERGE (xiaoyan:Entity:Character {name: "萧炎"})
MERGE (nalan:Entity:Character {name: "纳兰嫣然"})
MERGE (yunlanzong:Entity:Location {name: "云岚宗"})
MERGE (fofengnu:Entity:Skill {name: "佛怒火莲(雏形)"})

// 步骤 2: 创建事件枢纽节点，承载场景核心信息
CREATE (event:Event:StoryEvent:Combat:Duel {
    uuid: randomUUID(),
    chapterIndex: 150, // 假设章节
    eventType: 'Observation', 
    summary: "在三年之约中，萧炎于云岚宗广场通过佛怒火莲击败纳兰嫣然，洗刷了耻辱。",
    sourceText: "“嘭！”巨大的爆炸声响彻云霄，纳兰嫣然的身影如断线风筝般倒飞而出...",
    confidence: 0.98
})

// 步骤 3: 通过带属性的关系，将实体连接到事件，并附着瞬时描述信息
MERGE (xiaoyan)-[r1:PARTICIPATED_IN]->(event)
SET r1.role = "victor",
    r1.motivation = "洗刷耻辱",
    r1.emotion = "determined", // 情绪：坚决
    r1.quote = "三十年河东，三十年河西，莫欺少年穷！" // 关键台词

MERGE (nalan)-[r2:PARTICIPATED_IN {role: "loser", motivation: "履行约定"}]->(event)

// 步骤 4: 连接其他上下文实体
MERGE (event)-[:OCCURRED_AT]->(yunlanzong)
MERGE (event)-[:FEATURED_SKILL {usedBy: "萧炎", purpose: "终结战斗"}]->(fofengnu)

// 步骤 5 (如果适用): 描述事件导致的状态变更
// 假设此战让萧炎声名鹊起，这是一个状态变化
// 此处应调用6.3的状态转换模板，创建一个新的:State:ReputationState
// CREATE (newState:State {name: "名震加玛", ...})
// CREATE (event)-[:CREATES_STATE]->(newState)

```

---

## 6.5.6 建模补充：捕捉描述性与修饰性信息

1.  **瞬时描述 (通过关系属性):** 仅在事件发生期间有效的描述（如表情、对话、具体动作），应作为实体与事件之间**关系的属性**。
    -   **示例:** `(c)-[r:PARTICIPATED_IN {expression: "cold smile"}]->(e)`
2.  **持续性状态描述 (通过State节点属性):** 实体在一段时间内的持续状态（如地点氛围、角色心境），应作为**State节点的属性**。
    -   **示例:** `(s:State:LocationState {atmosphere: "ominous"})`
3.  **内在固有描述 (通过Entity节点属性):** 实体的永久性、定义性特征（如物品材质、先天体质），可作为**Entity节点本身的属性**（谨慎使用）。
    -   **示例:** `(i:Item {material: "extraterrestrial iron"})`

---

## 6.5.7 领域特定验证规则 (Domain-Specific Validation Rules)

1.  **实体唯一性:** `Entity.name` 在其 `entityType` 内部必须唯一。应始终使用 `MERGE` 操作实体。
2.  **状态连续性:** 任何实体的 `:State` 链条必须连续。一个状态的 `validToChapter` 必须等于下一个状态的 `validFromChapter`。
3.  **事件驱动:** 除结构性关系外，任何两个 `:Entity` 节点间不应存在直接的动态关系（如 `:ATTACKS`）。所有交互必须由 `:Event` 节点中介。
4.  **数据溯源:** 每个 `:Entity`, `:State`, `:Event` 节点都应尽可能包含 `sourceText` 属性。
5.  **属性完整性:**
    -   `Character` 的 `CultivationState` 必须包含 `level` 属性。
    -   `Item` 的 `PossessionState` 必须包含 `owner` 属性。
    -   `Event` 必须至少通过一个叙事性关系连接到一个 `:Entity`。
6.  **禁止信息孤岛:** 图中不应存在任何与其他节点没有关系的 `:Entity` 或 `:Event` 节点。

---

## 6.5.8 Cypher执行模板

#### 模板1: 创建角色及初始状态

```cypher
// 参数: $name, $alias, $chapter, $source, $initialState
MERGE (c:Entity:Character {name: $name})
ON CREATE SET
  c.uuid = randomUUID(),
  c.entityType = 'Character',
  c.alias = $alias,
  c.createdAtChapter = $chapter,
  c.firstMentionChapter = $chapter,
  c.sourceText = $source
WITH c
CREATE (e:Event:StoryEvent {
  uuid: randomUUID(),
  chapterIndex: $chapter,
  eventType: 'Generation',
  summary: '角色 ' + $name + ' 首次登场',
  sourceText: $source,
  confidence: 1.0
})
CREATE (s:State:CultivationState)
SET s += $initialState, // $initialState is a map, e.g., {level:'斗之气三段'}
    s.uuid = randomUUID(),
    s.validFromChapter = $chapter,
    s.validToChapter = null,
    s.sourceText = $source
CREATE (e)-[:GENERATES]->(c)
CREATE (e)-[:CREATES_STATE]->(s)
CREATE (c)-[:CURRENT_STATE]->(s)
```

#### 模板2: 角色状态转换 (调用6.3节原子化模板)

```cypher
// 参数: $entity_uuid, $event_chapter, $event_source, $newState_properties
MATCH (entity:Entity {uuid: $entity_uuid})-[rel:CURRENT_STATE]->(currentState:State)
SET currentState.validToChapter = $event_chapter
DELETE rel
CREATE (event:Event:StoryEvent {
    uuid: randomUUID(),
    chapterIndex: $event_chapter,
    eventType: 'Transformation',
    summary: '实体状态发生转变',
    sourceText: $event_source,
    confidence: 1.0
})
CREATE (newState:State)
SET newState += $newState_properties,
    newState.uuid = randomUUID(),
    newState.validFromChapter = $event_chapter,
    newState.validToChapter = null
CREATE (event)-[:TRANSFORMS]->(entity)
CREATE (event)-[:REQUIRES_STATE]->(currentState)
CREATE (event)-[:CREATES_STATE]->(newState)
CREATE (entity)-[:CURRENT_STATE]->(newState)
CREATE (entity)-[:HAS_HISTORICAL_STATE]->(currentState)
CREATE (currentState)-[:NEXT_STATE {transitionEventUuid: event.uuid}]->(newState)
```

#### 6.6 数据库级约束建议 (Optional but Recommended)

为了保证数据模型的绝对完整性，建议在Neo4j数据库中预先创建以下约束。这将防止任何（包括AI生成的）不合规的Cypher语句被执行。

```cypher
// 1. 保证所有核心元素的UUID是唯一的
CREATE CONSTRAINT entity_uuid_unique IF NOT EXISTS FOR (n:Entity) REQUIRE n.uuid IS UNIQUE;
CREATE CONSTRAINT event_uuid_unique IF NOT EXISTS FOR (n:Event) REQUIRE n.uuid IS UNIQUE;
CREATE CONSTRAINT state_uuid_unique IF NOT EXISTS FOR (n:State) REQUIRE n.uuid IS UNIQUE;

// 2. 保证领域实体的name唯一性
CREATE CONSTRAINT character_name_unique IF NOT EXISTS FOR (n:Character) REQUIRE n.name IS UNIQUE;
CREATE CONSTRAINT location_name_unique IF NOT EXISTS FOR (n:Location) REQUIRE n.name IS UNIQUE;
CREATE CONSTRAINT organization_name_unique IF NOT EXISTS FOR (n:Organization) REQUIRE n.name IS UNIQUE;

// 3. 保证一个实体在任何时候只能有一个 CURRENT_STATE
// (在Neo4j企业版中，可以通过关系属性唯一性约束实现。在社区版中，这需要应用层逻辑保证，
// 但我们的原子化Cypher模板已经强制了这一点。)
// CREATE CONSTRAINT one_current_state IF NOT EXISTS FOR ()-[r:CURRENT_STATE]-() REQUIRE r.unique_per_entity IS UNIQUE; 
// (注意: Neo4j目前不直接支持这种关系约束，但提及这个逻辑意图是有价值的)
```

#### **6.6 事务性与防御性Cypher模式**

生成的Cypher语句应天然地具备健壮性和原子性:

1. 单查询事务: 任何一个逻辑单元（如一次状态转换）都必须合并到一个单一的、多步骤的Cypher查询中。这确保了整个操作要么完全成功，要么完全失败，不会留下中间状态。（如优化后的场景2模板所示）。
2. 审慎使用 MERGE: MERGE 主要用于根据外部唯一键来创建或匹配Entity节点。对于每次操作都应是全新的Event和State节点，应始终使用CREATE，这样意图更明确，性能也更好。
3. 使用 WHERE 强制执行前置条件: 始终在 MATCH 子句后紧跟 WHERE 子句来验证前置条件。如果条件不满足，查询将不会返回任何行，从而自然地中止了后续的所有写操作。
4. 空值检查: 在访问属性或遍历可选关系之前，特别是跟在 OPTIONAL MATCH 之后，应检查变量是否存在。
```cypher
// 在遍历可选关系后检查是否存在
OPTIONAL MATCH (a)-[:SOME_REL]->(b)
WITH a, b
WHERE b IS NOT NULL
// 在这里可以安全地使用 b 的属性
...
```