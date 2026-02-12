package com.shuanglin.framework.onebot11.model.request;

import com.google.gson.annotations.SerializedName;
import com.shuanglin.framework.onebot11.exception.ValidationException;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DeleteFriendRequest implements ValidatableRequest {
    @SerializedName("user_id")
    private Long userId;

    @Override
    public void validate() {
        if (userId == null || userId <= 0) {
            throw new ValidationException("userId must be greater than 0", "userId");
        }
    }
}
