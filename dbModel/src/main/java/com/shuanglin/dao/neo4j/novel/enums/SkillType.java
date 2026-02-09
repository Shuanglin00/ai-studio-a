package com.shuanglin.dao.neo4j.novel.enums;

import lombok.Getter;

/**
 * 技能类型枚举
 * 对应SkillNode的skillType属性
 */
@Getter
public enum SkillType {
    
    /**
     * 功法
     */
    CULTIVATION_METHOD("功法", "CultivationMethod"),
    
    /**
     * 斗技
     */
    COMBAT_SKILL("斗技", "CombatSkill"),
    
    /**
     * 身法
     */
    MOVEMENT_SKILL("身法", "MovementSkill"),
    
    /**
     * 炼药术
     */
    ALCHEMY("炼药术", "Alchemy"),
    
    /**
     * 炼器术
     */
    REFINING("炼器术", "Refining"),
    
    /**
     * 符篆术
     */
    TALISMAN("符篆术", "Talisman"),
    
    /**
     * 阵法
     */
    FORMATION("阵法", "Formation"),
    
    /**
     * 灵魂功法
     */
    SOUL_SKILL("灵魂功法", "SoulSkill"),
    
    /**
     * 其他
     */
    OTHER("其他", "Other");
    
    /**
     * 中文名称
     */
    private final String zhName;
    
    /**
     * 英文代码
     */
    private final String enCode;
    
    SkillType(String zhName, String enCode) {
        this.zhName = zhName;
        this.enCode = enCode;
    }
    
    /**
     * 根据中文名称获取枚举
     */
    public static SkillType fromZhName(String zhName) {
        for (SkillType type : values()) {
            if (type.zhName.equals(zhName)) {
                return type;
            }
        }
        return OTHER;
    }
}
