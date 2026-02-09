package com.shuanglin.enums.book;

import lombok.Getter;

@Getter
public enum StyleEnum {
  FIRST_PERSON("第一人称", "以‘我’的视角叙述，增强代入感与主观性"),
  THIRD_PERSON_LIMITED("第三人称有限视角", "聚焦单一角色的内心与感知，但使用‘他/她’叙述"),
  THIRD_PERSON_OMNISCIENT("第三人称全知视角", "叙述者知晓所有角色的思想与事件全貌"),
  EPISODIC("片段式", "由多个相对独立的情节或章节组成，结构松散但主题统一"),
  LINEAR("线性叙事", "按时间顺序平铺直叙，结构清晰"),
  NON_LINEAR("非线性叙事", "采用倒叙、插叙、多时间线交错等方式"),
  MINIMALIST("极简主义", "语言简洁克制，避免修饰，留白多"),
  LUSH_PROSE("华丽文风", "辞藻丰富，描写细腻，注重语言美感"),
  SATIRICAL("讽刺风格", "通过夸张、反语等手法批判社会或人性"),
  LYRICAL("抒情风格", "富有诗意，强调情感与意境，节奏如诗"),
  HARD_BOILED("冷硬派", "语言干练、冷峻，常用于犯罪或 noir 风格小说"),
  STREAM_OF_CONSCIOUSNESS("意识流", "模仿人物内心思绪的流动，逻辑跳跃，时间模糊"),
  EPISTOLARY("书信体", "通过信件、日记、邮件等形式推进叙事"),
  UNRELIABLE_NARRATOR("不可靠叙述者", "叙述者有意或无意地误导读者，制造悬念或反转");

  private final String displayName;
  private final String description;

  StyleEnum(String displayName, String description) {
    this.displayName = displayName;
    this.description = description;
  }

  @Override
  public String toString() {
    return this.name() + " (" + displayName + ")";
  }
}
