package com.gdufe.mall.common.security;

import com.gdufe.mall.common.exception.BizException;
import com.gdufe.mall.common.result.ResultCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 取当前登录用户。所有"我的 XX"接口必须用它拿 userId，禁止信任前端传的 userId。
 */
public final class SecurityUtil {

    private SecurityUtil() {
    }

    public static LoginUser current() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof LoginUser user)) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
        return user;
    }

    public static Long currentUserId() {
        return current().getId();
    }

    public static boolean isAdmin() {
        return "ADMIN".equals(current().getRole());
    }
}
