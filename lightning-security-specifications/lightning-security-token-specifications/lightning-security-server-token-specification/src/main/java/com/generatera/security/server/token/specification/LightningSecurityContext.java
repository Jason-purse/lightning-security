package com.generatera.security.server.token.specification;

import org.springframework.security.core.Authentication;

/**
 * @author FLJ
 * @date 2023/1/3
 * @time 9:38
 * @Description 安全上下文 ...
 */
public interface LightningSecurityContext {

    /**
     * 认证信息
     */
    Authentication getAuthentication();

}