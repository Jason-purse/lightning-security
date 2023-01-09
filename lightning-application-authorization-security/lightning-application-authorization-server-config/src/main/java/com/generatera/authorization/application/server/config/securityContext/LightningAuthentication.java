package com.generatera.authorization.application.server.config.securityContext;

import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.Authentication;

/**
 * @author FLJ
 * @date 2023/1/5
 * @time 11:21
 * @Description Lightning Authentication
 */
public interface LightningAuthentication extends Authentication {

    /**
     * 获取解析器Class
     */
    @NotNull
    Class<? extends LightningAuthenticationParser> getParserClass();
}
