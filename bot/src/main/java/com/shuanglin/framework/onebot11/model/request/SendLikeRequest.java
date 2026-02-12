package com.shuanglin.framework.onebot11.model.request;

import com.google.gson.annotations.SerializedName;
import com.shuanglin.framework.onebot11.exception.ValidationException;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SendLikeRequest implements ValidatableRequest {
    @SerializedName("user_id")
    private Long userId;
    @Builder.Default
    private Integer times = 1;

    @Override
    public void validate() {
        if (userId == null || userId <= 0) {
            throw new ValidationException("userId must be greater than 0", "userId");
        }
        if (times == null || times < 1 || times > 10) {
            throw new ValidationException("times must be between 1 and 10", "times");
        }
    }
}
