package com.shuanglin.framework.milky.model.response;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 群公告列表响应
 */
@Data
public class GroupAnnouncementListResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 公告列表
     */
    private List<GroupAnnouncement> announcements;
}
