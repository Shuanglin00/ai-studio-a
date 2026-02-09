package com.shuanglin.framework.milky.model.response;

import com.shuanglin.framework.milky.enums.MilkyStatus;
import lombok.Data;

import java.io.Serializable;

/**
 * Milky API 通用响应包装
 */
@Data
public class MilkyResponse<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 响应状态: ok | failed
     */
    private MilkyStatus status;

    /**
     * 返回码: 0 表示成功
     */
    private Integer retcode;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 判断是否成功
     */
    public boolean isOk() {
        return MilkyStatus.OK.equals(status) && retcode != null && retcode == 0;
    }

    /**
     * 获取数据，如果失败则返回null
     */
    public T getDataOrNull() {
        return isOk() ? data : null;
    }
}
