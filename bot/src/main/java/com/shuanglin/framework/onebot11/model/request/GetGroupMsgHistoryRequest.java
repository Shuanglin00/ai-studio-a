package com.shuanglin.framework.onebot11.model.request;

import com.shuanglin.framework.onebot11.exception.ValidationException;
import lombok.Builder;
import lombok.Data;

/**
 * 获取群消息历史记录请求
 */
@Data
@Builder
public class GetGroupMsgHistoryRequest implements ValidatableRequest {

    /**
     * 群号
     */
    private Long groupId;

    /**
     * 起始消息序号（0 表示从最后一条消息开始）
     */
    @Builder.Default
    private Long messageSeq = 0L;

    /**
     * 消息数量（默认 20，最大 20）
     */
    @Builder.Default
    private Integer count = 20;

    @Override
    public void validate() {
        if (groupId == null || groupId <= 0) {
            throw new ValidationException("groupId 必须大于 0", "groupId");
        }
        if (count == null || count <= 0 || count > 20) {
            throw new ValidationException("count 必须在 1-20 之间", "count");
        }
    }
}
