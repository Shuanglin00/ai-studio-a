package com.shuanglin.framework.bus.event.data;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * 匿名消息信息
 */
@Data
public class Anonymous {
    @SerializedName("id")
    private Long id;          // 匿名用户 ID

    @SerializedName("name")
    private String name;      // 匿名用户名称

    @SerializedName("flag")
    private String flag;      // 匿名标志
}
