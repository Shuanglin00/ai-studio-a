package com.shuanglin.framework.onebot.segment;

/**
 * 图片消息段
 * 支持本地文件(file://)、网络URL(http://)、Base64(base64://)
 */
public class ImageSegment extends MessageSegment {

    public ImageSegment(String file) {
        this.type = "image";
        this.data.put("file", file);
    }

    @Override
    public void validate() {
        if (data.get("file") == null) {
            throw new IllegalArgumentException("Image file cannot be null");
        }
        
        String file = data.get("file").toString();
        if (!file.startsWith("file://") && 
            !file.startsWith("http://") && 
            !file.startsWith("https://") && 
            !file.startsWith("base64://")) {
            throw new IllegalArgumentException("Invalid image file format");
        }
    }
}
