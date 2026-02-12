package com.shuanglin.framework.onebot11.model.request;

import com.shuanglin.framework.onebot11.exception.ValidationException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 获取精华消息列表请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetEssenceMsgListRequest implements ValidatableRequest {

    /**
     * 群号
     */
    private Long groupId;

    @Override
    public void validate() {
        if (groupId == null || groupId <= 0) {
            throw new ValidationException("groupId 必须大于 0", "groupId");
        }
    }
}
