package com.shuanglin.ai.novel.enums;

import lombok.Getter;

@Getter
public enum EntityTypeEnum {

  // --- 核心生命体 ---
  CHARACTER("Character", "角色", "人名、角色名、主角、配角"),
  SPECIES("Species", "种族/物种", "人类、妖族、灵族、魔族、古神"),
  IDENTITY("Identity", "身份/称号", "称号、头衔、职位、尊号"),
  PROFESSION("Profession", "职业", "炼丹师、阵法师、剑修、符师"),

  // --- 空间与时间 ---
  WORLD("World", "位面/世界", "大世界、小世界、位面、空间层级"),
  LOCATION("Location", "地点/场景", "地名、山脉、禁地、城市、秘境"),
  ERA("Era", "时代/纪元", "历史时期、上古时代、黄金纪元"),

  // --- 组织与社会 ---
  ORGANIZATION("Organization", "组织/势力", "宗门、家族、国家、商会、秘密结社"),
  RULE("Rule", "规则/制度", "世界底层逻辑、天道规则、门规、法律"),
  CURRENCY("Currency", "货币/资源", "灵石、仙元、功德值、气运点"),

  // --- 修炼与力量体系 ---
  REALM("Realm", "境界/等级", "修炼等级、肉身强度、神魂等级"),
  SKILL("Skill", "技能/功法", "法术、神通、功法、阵图、秘策"),
  CONSTITUTION("Constitution", "体质/血脉", "特殊体质、血脉传承、神体"),
  LEGACY("Legacy", "传承", "远古传承、大帝遗泽、道统"),

  // --- 物质与道具 ---
  ITEM("Item", "物品/法宝", "武器、防具、丹药、神兵"),
  MATERIAL("Material", "材料/草药", "炼器材料、灵草、妖丹、矿石"),

  // --- 宏观现象与抽象概念 ---
  CONCEPT("Concept", "抽象概念", "气运、业力、因果、天命"),
  PHENOMENON("Phenomenon", "天地异象", "天劫、异象、灵气复苏、潮汐"),
  EVENT("Event", "事件/战役", "历史事件、著名战役、宗门大比、封神之战"),

  // --- 能量与属性 ---
  ELEMENT("Element", "属性/能量", "五行属性、阴阳、魔气、混沌之力");

  private final String label;       // 对应 Neo4j 中的 Label
  private final String name;        // 中文名称
  private final String description; // 详细描述/提取示例

  EntityTypeEnum(String label, String name, String description) {
    this.label = label;
    this.name = name;
    this.description = description;
  }

  /**
   * 根据 label 字符串获取枚举
   */
  public static EntityTypeEnum fromLabel(String label) {
    for (EntityTypeEnum type : EntityTypeEnum.values()) {
      if (type.getLabel().equalsIgnoreCase(label)) {
        return type;
      }
    }
    return null;
  }
}