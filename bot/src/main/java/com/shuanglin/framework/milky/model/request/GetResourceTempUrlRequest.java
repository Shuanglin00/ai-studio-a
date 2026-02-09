package com.shuanglin.framework.milky.model.request;
import lombok.AllArgsConstructor;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 获取临时资源链接请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetResourceTempUrlRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 资源ID
     */
    private String resourceId;
}
