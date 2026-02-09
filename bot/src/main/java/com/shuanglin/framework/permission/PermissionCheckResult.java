package com.shuanglin.framework.permission;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 权限校验结果
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PermissionCheckResult {

    private boolean success;
    private String reason;

    public static PermissionCheckResult success() {
        return new PermissionCheckResult(true, null);
    }

    public static PermissionCheckResult fail(String reason) {
        return new PermissionCheckResult(false, reason);
    }

    public boolean isSuccess() {
        return success;
    }
}
