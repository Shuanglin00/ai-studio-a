package com.shuanglin.framework.onebot.segment;

import lombok.Getter;

/**
 * 文件消息段
 */
@Getter
public class FileSegment extends MessageSegment {

    public FileSegment(String file) {
        this.type = "file";
        this.data.put("file", file);
    }

    public void setUrl(String url) {
        this.data.put("url", url);
    }

    public void setPath(String path) {
        this.data.put("path", path);
    }

    public void setFileSize(String fileSize) {
        this.data.put("file_size", fileSize);
    }

    public void setFileId(String fileId) {
        this.data.put("file_id", fileId);
    }

    public void setThumb(String thumb) {
        this.data.put("thumb", thumb);
    }

    public void setName(String name) {
        this.data.put("name", name);
    }

    @Override
    public void validate() {
        if (data.get("file") == null) {
            throw new IllegalArgumentException("File cannot be null");
        }
    }
}
