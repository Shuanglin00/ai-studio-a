package com.shuanglin.bot.novel;

public class NovelPrompt {
  public final static String entityScannerPrompt = """
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
          """;
}
