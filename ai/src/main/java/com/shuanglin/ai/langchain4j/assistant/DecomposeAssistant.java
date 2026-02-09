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
	 * @param textChunk 全书文本片段
	 * @param bookName 书籍名称
	 * @return JSON格式的实体列表
	 */
	@UserMessage("""
			# 任务：全局实体提取
			
			请从以下文本中提取所有实体，包括角色、地点、组织、物品、技能。
			
			## 提取规则
			1. **专有名词优先**：仅提取具有明确指代的专有名词（如"萧炎"、"云岚宗"、"玄重尺"）
			2. **别名聚合**：识别同一实体的所有别名（如"萧炎"的别名："岩枭"、"萧家三少爷"、"炎帝"）
			3. **过滤泛指词**：忽略泛指词（如"黑衣人"、"老者"、"那个人"）
			4. **首次出现标记**：记录实体首次出现的位置
			
			## 输出格式
			
			请返回JSON数组，每个元素包含以下字段：
			```json
			{
			  "standardName": "实体标准名称",
			  "aliases": ["别名1", "别名2"],
			  "entityType": "Character/Location/Organization/Item/Skill",
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
}
