package com.shuanglin.framework.bus.event;

import com.google.gson.annotations.SerializedName;
import com.shuanglin.framework.bus.event.data.Anonymous;
import lombok.*;

import java.io.Serializable;

/**
 * 群消息事件
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
public class GroupMessageEvent extends MessageEvent implements Serializable {

    @SerializedName("anonymous")
    private Anonymous anonymous;  // 匿名信息（如果消息是匿名的）
}
