package com.shuanglin.framework.onebot11.model.request;

import com.google.gson.annotations.SerializedName;
import com.shuanglin.framework.onebot11.exception.ValidationException;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 获取群信息请求
 */
@Data
@AllArgsConstructor
public class GetGroupInfoRequest implements ValidatableRequest {

    /**
     * 群号
     */
    @SerializedName("group_id")
    private Long groupId;

    /**
     * 是否不使用缓存（使用缓存可能更新不及时，但响应更快）
     */
    @SerializedName("no_cache")
    private Boolean noCache;

    @Override
    public void validate() {
        if (groupId == null || groupId <= 0) {
            throw new ValidationException("groupId 必须大于 0", "groupId");
        }
    }
}
