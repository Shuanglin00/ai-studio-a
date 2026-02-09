package com.shuanglin.dao.neo4j.novel.enums;

import lombok.Getter;

/**
 * 地点类型枚举
 * 对应LocationNode的locationType属性
 */
@Getter
public enum LocationType {
    
    /**
     * 城市
     */
    CITY("城市", "City"),
    
    /**
     * 宗门
     */
    SECT("宗门", "Sect"),
    
    /**
     * 山脉
     */
    MOUNTAIN("山脉", "Mountain"),
    
    /**
     * 秘境
     */
    SECRET_REALM("秘境", "SecretRealm"),
    
    /**
     * 帝国
     */
    EMPIRE("帝国", "Empire"),
    
    /**
     * 村镇
     */
    TOWN("村镇", "Town"),
    
    /**
     * 洞府
     */
    CAVE("洞府", "Cave"),
    
    /**
     * 遗迹
     */
    RUINS("遗迹", "Ruins"),
    
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
    
    LocationType(String zhName, String enCode) {
        this.zhName = zhName;
        this.enCode = enCode;
    }
    
    /**
     * 根据中文名称获取枚举
     */
    public static LocationType fromZhName(String zhName) {
        for (LocationType type : values()) {
            if (type.zhName.equals(zhName)) {
                return type;
            }
        }
        return OTHER;
    }
}
