package com.shuanglin.framework.onebot11.model.request;

import com.shuanglin.framework.onebot11.exception.ValidationException;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HandleFriendRequestRequest implements ValidatableRequest {
    private String flag;
    @Builder.Default
    private Boolean approve = true;
    private String remark;

    @Override
    public void validate() {
        if (flag == null || flag.isEmpty()) {
            throw new ValidationException("flag cannot be empty", "flag");
        }
    }
}
