package com.shuanglin.dao.neo4j.novel.enums;

import lombok.Getter;

/**
 * 组织类型枚举
 * 对应OrganizationNode的orgType属性
 */
@Getter
public enum OrganizationType {
    
    /**
     * 家族
     */
    FAMILY("家族", "Family"),
    
    /**
     * 帝国
     */
    EMPIRE("帝国", "Empire"),
    
    /**
     * 佣兵团
     */
    MERCENARY_GROUP("佣兵团", "MercenaryGroup"),
    
    /**
     * 宗门
     */
    SECT("宗门", "Sect"),
    
    /**
     * 商会
     */
    MERCHANT_GUILD("商会", "MerchantGuild"),
    
    /**
     * 学院
     */
    ACADEMY("学院", "Academy"),
    
    /**
     * 帮派
     */
    GANG("帮派", "Gang"),
    
    /**
     * 联盟
     */
    ALLIANCE("联盟", "Alliance"),
    
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
    
    OrganizationType(String zhName, String enCode) {
        this.zhName = zhName;
        this.enCode = enCode;
    }
    
    /**
     * 根据中文名称获取枚举
     */
    public static OrganizationType fromZhName(String zhName) {
        for (OrganizationType type : values()) {
            if (type.zhName.equals(zhName)) {
                return type;
            }
        }
        return OTHER;
    }
}
