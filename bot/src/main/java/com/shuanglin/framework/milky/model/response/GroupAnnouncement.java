package com.shuanglin.framework.milky.model.response;

import lombok.Data;

import java.io.Serializable;

/**
 * 群公告
 */
@Data
public class GroupAnnouncement implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 公告ID
     */
    private String announcementId;

    /**
     * 公告内容
     */
    private String content;

    /**
     * 发布时间
     */
    private Long publishTime;

    /**
     * 发布者ID
     */
    private Long publisherId;

    /**
     * 是否显示封面
     */
    private Boolean showEditInfo;

    /**
     * 是否置顶
     */
    private Boolean isTop;

    /**
     * 弹窗显示
     */
    private Boolean confirmRequired;

    /**
     * 类型
     */
    private Integer type;
}
