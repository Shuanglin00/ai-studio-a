package com.shuanglin.framework.onebot11.model.response;

import lombok.Data;

/**
 * OneBot 11 标准响应格式
 * 所有 API 响应的基础包装类
 *
 * @param <T> 响应数据类型
 * @author Shuanglin
 * @since 1.0
 */
@Data
public class OneBotResponse<T> {

    /**
     * 状态：ok / failed / async
     */
    private String status;

    /**
     * 返回码：0 表示成功
     */
    private Integer retcode;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 错误信息
     */
    private String message;

    /**
     * 用户友好的错误信息
     */
    private String wording;

    /**
     * Echo 字段（用于异步请求匹配）
     */
    private String echo;

    /**
     * 是否成功
     *
     * @return true 如果 status 为 "ok" 且 retcode 为 0
     */
    public boolean isSuccess() {
        return "ok".equals(status) && retcode != null && retcode == 0;
    }

    /**
     * 是否失败
     *
     * @return true 如果 status 为 "failed"
     */
    public boolean isFailed() {
        return "failed".equals(status) || (retcode != null && retcode != 0);
    }

    /**
     * 是否异步
     *
     * @return true 如果 status 为 "async"
     */
    public boolean isAsync() {
        return "async".equals(status);
    }

    /**
     * 获取错误消息（优先使用 wording）
     *
     * @return 错误消息
     */
    public String getErrorMessage() {
        if (wording != null && !wording.isEmpty()) {
            return wording;
        }
        return message;
    }
}
