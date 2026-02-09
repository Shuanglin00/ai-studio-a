package com.shuanglin.framework.milky.model.request;
import lombok.AllArgsConstructor;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 设置群公告请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SetGroupAnnouncementRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 群ID
     */
    private Long groupId;

    /**
     * 公告内容
     */
    private String content;

    /**
     * 是否置顶
     */
    private Boolean isTop;

    /**
     * 弹窗显示
     */
    private Boolean confirmRequired;

    /**
     * 显示编辑信息
     */
    private Boolean showEditInfo;
}
