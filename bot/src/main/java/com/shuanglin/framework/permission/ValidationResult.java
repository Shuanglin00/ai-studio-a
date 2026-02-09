package com.shuanglin.framework.permission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 验证结果模型
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ValidationResult {

    /**
     * 是否验证通过
     */
    private boolean success;

    /**
     * 失败原因
     */
    private String reason;

    /**
     * 创建成功结果
     */
    public static ValidationResult success() {
        return ValidationResult.builder()
                .success(true)
                .build();
    }

    /**
     * 创建失败结果
     */
    public static ValidationResult fail(String reason) {
        return ValidationResult.builder()
                .success(false)
                .reason(reason)
                .build();
    }
}
