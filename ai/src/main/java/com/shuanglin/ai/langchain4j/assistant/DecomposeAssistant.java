package com.shuanglin.ai.langchain4j.assistant;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface DecomposeAssistant {

	@UserMessage("将以下问题分解为3个或更少的、更简单的子问题。只返回一个以；分号分隔的子问题列表，不要添加任何其他文本。问题：{{query}}")
	String decompose(@V("query") String query);

	/**
	 * 通用知识图谱生成方法，使用 kgKnowlage.md 作为 System Prompt
	 * @param userPrompt 用户提示词（任务上下文、操作指南、示例）- 已完成变量替换的完整文本
	 * @return Cypher语句
	 */
	String generateCypher(String userPrompt);
	
	@UserMessage("""
			你是一个知识图谱构建助手，专门用于将小说文本转换为 Neo4j Cypher 插入语句。
			
			**上下文信息：**
			上一章完整内容：　　{{lastContext}}
			当前章完整内容：　　{{indexText}}
			下一章完整内容：　　{{nextContext}}
			
			**任务说明：**
			请基于完整的上下文信息，分析当前章的内容，提取其中的：
			1. 实体（人物、地点、物品、技能、状态等）
			2. 实体间的关系
			3. 实体的属性
			
			**生成要求：**
			1. 只生成 Neo4j Cypher 语句，不添加任何解释
			2. 使用 MERGE 避免重复创建节点和关系
			3. 节点标签：:Character, :Location, :Item, :Skill, :State, :Event
			4. 关系类型：使用英文大写（如 :LOCATED_IN, :USES, :LEARNS, :HAS, :CONTAINS）
			5. 属性使用中文键名（name, 描述, 等级 等）
			6. 如果当前章没有可提取的新信息，返回空字符串
			
			**示例输出格式：**
			MERGE (c:Character {name: "萧炎"})\s
			MERGE (s:State {name: "四段斗之气"})\s
			MERGE (c)-[:HAS_STATE]->(s)
			
			请开始生成：
			""")
	String enhancedEntityExtraction(@V("indexText") String indexText, @V("lastContext") String lastContext, @V("nextContext") String nextContext);
	
	// ==================== 精准还原功能扩展方法 ====================
	
	/**
	 * 全局实体提取（阶段一）
	 * 支持20种细粒度实体类型
	 * @param textChunk 全书文本片段
	 * @return JSON格式的实体列表
	 */
	@UserMessage("""
			# 任务：全局实体提取

			请从以下文本中提取所有实体。

			## 实体类型（20种）

			### 核心生命体
			- CHARACTER（角色）：人名、角色名、主角、主角、配角
			- SPECIES（种族/物种）：人类、妖族、灵族、魔族、古神
			- IDENTITY（身份/称号）：称号、头衔、职位、尊号
			- PROFESSION（职业）：炼丹师、阵法师、剑修、符师

			### 空间与时间
			- WORLD（位面/世界）：大世界、小世界、位面、空间层级
			- LOCATION（地点/场景）：地名、山脉、禁地、城市、秘境
			- ERA（时代/纪元）：历史时期、上古时代、黄金纪元

			### 组织与社会
			- ORGANIZATION（组织/势力）：宗门、家族、国家、商会、秘密结社
			- RULE（规则/制度）：世界底层逻辑、天道规则、门规、法律
			- CURRENCY（货币/资源）：灵石、仙元、功德值、气运点

			### 修炼与力量体系
			- REALM（境界/等级）：修炼等级、肉身强度、神魂等级
			- SKILL（技能/功法）：法术、神通、功法、阵图、秘策
			- CONSTITUTION（体质/血脉）：特殊体质、血脉传承、神体
			- LEGACY（传承）：远古传承、大帝遗泽、道统

			### 物质与道具
			- ITEM（物品/法宝）：武器、防具、丹药、神兵
			- MATERIAL（材料/草药）：炼器材料、灵草、妖丹、矿石

			### 宏观现象与抽象概念
			- CONCEPT（抽象概念）：气运、业力、因果、天命
			- PHENOMENON（天地异象）：天劫、异象、灵气复苏、潮汐
			- EVENT（事件/战役）：历史事件、著名战役、宗门大比、封神之战

			### 能量与属性
			- ELEMENT（属性/能量）：五行属性、阴阳、魔气、混沌之力

			## 提取规则
			1. **专有名词优先**：仅提取具有明确指代的专有名词（如"萧炎"、"云岚宗"、"玄重尺"）
			2. **别名聚合**：识别同一实体的所有别名（如"萧炎"的别名："岩枭"、"萧家三少爷"、"炎帝"）
			3. **过滤泛指词**：忽略泛指词（如"黑衣人"、"老者"、"那个人"）
			4. **类型精确**：根据实体特征选择最精确的类型

			## 输出格式

			请返回JSON数组，每个元素包含以下字段：
			```json
			{
			  "standardName": "实体标准名称",
			  "aliases": ["别名1", "别名2"],
			  "entityType": "20种类型之一，如 CHARACTER, LOCATION, ORGANIZATION, ITEM, SKILL, REALM 等",
			  "firstMention": "首次出现的文本片段"
			}
			```

			## 文本内容

			{{textChunk}}
			""")
	String extractGlobalEntities(@V("textChunk") String textChunk);
	
	/**
	 * 句子级内容分解（阶段二）
	 * @param chapterText 章节文本
	 * @param entityRegistry 实体注册表JSON
	 * @return JSON格式的句子单元列表
	 */
	@UserMessage("""
			# 任务：句子级内容分解
			
			请将以下章节文本按句子分解，并为每个句子分类和提取结构化数据。
			
			## 句子分类标准
			
			1. **Dialogue（对话）**：角色说话内容，包含引号或"道"、"说"等动词
			   - 提取：speaker（说话人实体ID）、content（对话内容）、tone（语气）
			
			2. **Action（动作）**：角色执行的具体动作
			   - 提取：subject（动作主体实体ID）、verb（动词）、object（动作客体实体ID）、manner（方式）
			
			3. **Description（描写）**：环境、外貌、情感等描述性内容
			   - 提取：descriptionType（environment/appearance/emotion）、target（被描写的实体ID）
			
			4. **Thought（心理活动）**：角色的内心想法
			   - 提取：thinker（思考者实体ID）、content（心理活动内容）
			
			5. **StateChange（状态变化）**：实体属性变化（如境界突破、受伤）
			   - 提取：entity（实体ID）、stateType（状态类型）、stateValue（新状态值）
			
			6. **Narration（叙述）**：时间过渡、场景转换、说明性文字
			   - 提取：narrativeType（time_transition/scene_change/explanation）
			
			## 实体注册表
			
			以下是本书的实体标准库，请使用entityId引用实体：
			
			{{entityRegistry}}
			
			## 输出格式
			
			返回JSON数组，每个元素结构：
			```json
			{
			  "sentenceType": "Dialogue/Action/Description/Thought/StateChange/Narration",
			  "originalText": "完整原文",
			  "entities": ["涉及的实体ID列表"],
			  "structuredData": {
			    // 根据句子类型填充对应字段
			  }
			}
			```
			
			## 章节文本

			{{chapterText}}
			""")
	String decomposeSentences(@V("chapterText") String chapterText, @V("entityRegistry") String entityRegistry);

	/**
	 * 实体一致性验证（阶段三）
	 * 用于验证已识别实体的一致性和正确性
	 * @param entityList 实体列表JSON
	 * @param contextText 上下文文本片段
	 * @return 验证结果JSON
	 */
	@UserMessage("""
			# 任务：实体一致性验证

			请验证以下实体列表的一致性和正确性。

			## 验证要求
			1. **重复检测**：检测是否有重复实体（名称相同或别名相同）
			2. **类型验证**：验证实体类型是否正确
			3. **别名验证**：验证别名是否合理（不应包含泛指词）
			4. **一致性检查**：验证同一实体在不同章节的提及是否一致

			## 实体列表

			{{entityList}}

			## 上下文片段

			{{contextText}}

			## 输出格式

			返回JSON数组，每个元素包含以下字段：
			```json
			{
			  "entityId": "实体ID",
			  "standardName": "标准名称",
			  "isValid": true/false,
			  "issues": ["问题1", "问题2"],
			  "suggestedCorrection": {
			    "newStandardName": "建议的标准名称",
			    "newAliases": ["新别名列表"],
			    "newEntityType": "新实体类型"
			  }
			}
			```

			如果实体没有问题，issues字段为空数组。

			请返回完整的验证结果：
			""")
	String verifyEntities(@V("entityList") String entityList, @V("contextText") String contextText);

	/**
	 * 事件验证（阶段三）
	 * 用于验证事件-实体关联的合理性
	 * @param eventList 事件列表JSON
	 * @param entityRegistry 实体注册表JSON
	 * @return 验证结果JSON
	 */
	@UserMessage("""
			# 任务：事件-实体关联验证

			请验证以下事件列表的合理性和一致性。

			## 验证要求
			1. **实体引用验证**：验证事件中的实体引用是否有效
			2. **事件类型验证**：验证事件类型是否合理
			3. **时序一致性**：验证事件时序是否合理
			4. **因果关系验证**：验证事件的因果关系是否合理

			## 事件列表

			{{eventList}}

			## 实体注册表

			{{entityRegistry}}

			## 输出格式

			返回JSON数组，每个元素包含以下字段：
			```json
			{
			  "eventUuid": "事件UUID",
			  "chapterIndex": 1,
			  "eventType": "BATTLE",
			  "isValid": true/false,
			  "issues": ["问题1", "问题2"],
			  "confidence": 0.95
			}
			```

			请返回完整的事件验证结果：
			""")
	String verifyEvents(@V("eventList") String eventList, @V("entityRegistry") String entityRegistry);

	/**
	 * 跨章节实体追踪（阶段三）
	 * 用于追踪实体在不同章节的变化
	 * @param entityName 实体名称
	 * @param chapterTextPairs 章节文本对列表 [{"chapter": 1, "text": "..."}]
	 * @return 追踪结果JSON
	 */
	@UserMessage("""
			# 任务：跨章节实体追踪与消歧

			请追踪以下实体在不同章节的提及，分析其一致性并消歧。

			## 实体名称
			{{entityName}}

			## 章节文本对

			{{chapterTextPairs}}

			## 追踪要求
			1. 分析实体在不同章节的出现是否指向同一实体
			2. 检测是否有歧义（如重名实体）
			3. 如果发现多个可能的实体，进行消歧
			4. 分析实体的变化轨迹

			## 输出格式

			返回JSON对象：
			```json
			{
			  "entityName": "实体名称",
			  "isConsistent": true/false,
			  "possibleEntities": [
			    {
			      "id": "实体ID",
			      "evidence": ["支持证据1", "支持证据2"],
			      "firstChapter": 1,
			      "lastChapter": 10
			    }
			  ],
			  "disambiguationNotes": "消歧说明"
			}
			```

			请返回追踪结果：
			""")
	String trackEntityAcrossChapters(@V("entityName") String entityName,
								   @V("chapterTextPairs") String chapterTextPairs);

	/**
	 * 实体描述生成（阶段四）
	 * 为实体生成描述文本，用于增强生成
	 * @param entityName 实体名称
	 * @param entityType 实体类型
	 * @param events 实体相关事件JSON
	 * @return 实体描述
	 */
	@UserMessage("""
			# 任务：实体描述生成

			请基于以下信息生成实体的描述文本。

			## 实体信息
			- 名称：{{entityName}}
			- 类型：{{entityType}}

			## 相关事件

			{{events}}

			## 要求
			1. 描述应该简洁但信息丰富
			2. 包含实体的关键特征和重要事件
			3. 长度控制在100字以内

			请返回描述文本：
			""")
	String generateEntityDescription(@V("entityName") String entityName,
									@V("entityType") String entityType,
									@V("events") String events);
}
