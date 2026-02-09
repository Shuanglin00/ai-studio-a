package com.shuanglin.enums.book;


import lombok.Getter;

@Getter
public enum ClassifyEnum {
  FANTASY("奇幻", "包含魔法、神话生物、架空世界等元素的小说"),
  SCIENCE_FICTION("科幻", "以科学、技术、未来社会或外星文明为背景的小说"),
  ROMANCE("言情", "以爱情关系为核心情节的小说"),
  MYSTERY("悬疑", "围绕谜团、犯罪或未解事件展开推理的小说"),
  THRILLER("惊悚", "强调紧张、危险和心理压迫感的小说"),
  HORROR("恐怖", "旨在引发恐惧、不安或惊吓的小说"),
  HISTORICAL("历史", "以真实历史时期为背景，可能融合虚构人物或事件"),
  ADVENTURE("冒险", "主角经历一系列危险旅程或探索的小说"),
  DRAMA("剧情", "聚焦人物情感、人际关系与生活冲突的现实主义小说"),
  COMEDY("喜剧", "以幽默、滑稽或讽刺为主要风格的小说"),
  MARTIAL_ARTS("武侠", "以中国武术、江湖恩怨、侠义精神为主题的小说"),
  XIANXIA("仙侠", "融合道教修仙、神仙体系与中国传统文化的东方奇幻小说"),
  URBAN("都市", "以现代城市为背景，涵盖职场、生活、情感等题材"),
  MILITARY("军事", "描写战争、军队生活或战略战术的小说"),
  GAME("游戏", "以虚拟游戏世界或电竞为背景的小说"),
  ESPORTS("电竞", "聚焦电子竞技选手成长与比赛的故事"),
  SHORT_STORY("短篇", "篇幅较短、结构紧凑的叙事作品"),
  LITERARY_FICTION("纯文学", "注重语言艺术、思想深度与人性探讨的小说");

  private final String displayName;
  private final String description;

  ClassifyEnum(String displayName, String description) {
    this.displayName = displayName;
    this.description = description;
  }

  @Override
  public String toString() {
    return this.name() + " (" + displayName + ")";
  }
}
