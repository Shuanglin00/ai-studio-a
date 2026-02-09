package com.shuanglin.dao.neo4j.novel.enums;

import lombok.Getter;

/**
 * 物品类型枚举
 * 对应ItemNode的itemType属性
 */
@Getter
public enum ItemType {
    
    /**
     * 武器
     */
    WEAPON("武器", "Weapon"),
    
    /**
     * 丹药
     */
    PILL("丹药", "Pill"),
    
    /**
     * 药材
     */
    HERB("药材", "Herb"),
    
    /**
     * 防具
     */
    ARMOR("防具", "Armor"),
    
    /**
     * 异火
     */
    STRANGE_FIRE("异火", "StrangeFire"),
    
    /**
     * 灵宝
     */
    TREASURE("灵宝", "Treasure"),
    
    /**
     * 材料
     */
    MATERIAL("材料", "Material"),
    
    /**
     * 卷轴
     */
    SCROLL("卷轴", "Scroll"),
    
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
    
    ItemType(String zhName, String enCode) {
        this.zhName = zhName;
        this.enCode = enCode;
    }
    
    /**
     * 根据中文名称获取枚举
     */
    public static ItemType fromZhName(String zhName) {
        for (ItemType type : values()) {
            if (type.zhName.equals(zhName)) {
                return type;
            }
        }
        return OTHER;
    }
}
