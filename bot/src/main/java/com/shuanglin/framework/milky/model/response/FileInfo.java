package com.shuanglin.framework.milky.model.response;

import lombok.Data;

import java.io.Serializable;

/**
 * 文件信息
 */
@Data
public class FileInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 文件ID
     */
    private String fileId;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * 上传时间
     */
    private Long uploadTime;

    /**
     * 过期时间
     */
    private Long deadTime;

    /**
     * 下载次数
     */
    private Integer downloadTimes;

    /**
     * 父目录ID
     */
    private String parentId;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 发送者ID
     */
    private Long senderId;

    /**
     * 发送者名称
     */
    private String senderName;

    /**
     * _BUSINESS_   文件业务类型
     */
    private String business;

    /**
     * 文件URL
     */
    private String url;
}
