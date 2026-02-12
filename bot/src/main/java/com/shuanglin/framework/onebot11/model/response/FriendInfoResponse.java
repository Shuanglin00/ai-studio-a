package com.shuanglin.framework.onebot11.model.response;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class FriendInfoResponse {
    @SerializedName("user_id")
    private Long userId;
    private String nickname;
    private String remark;
}
