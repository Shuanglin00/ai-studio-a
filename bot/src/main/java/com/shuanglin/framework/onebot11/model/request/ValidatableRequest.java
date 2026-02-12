package com.shuanglin.framework.onebot11.model.request;

/**
 * 可校验请求接口
 * 所有请求 DTO 应实现此接口以提供参数校验功能
 *
 * @author Shuanglin
 * @since 1.0
 */
@FunctionalInterface
public interface ValidatableRequest {

    /**
     * 校验请求参数
     * 如果校验失败，应抛出 ValidationException
     */
    void validate();
}
