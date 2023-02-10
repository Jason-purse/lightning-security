package com.generatera.security.authorization.server.specification.components.authentication;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.SecurityContextRepository;

import javax.servlet.http.HttpServletRequest;

/**
 * @author FLJ
 * @date 2023/1/12
 * @time 9:58
 * @Description 主要是实现   SecurityContext 上下文的加载 ...
 *
 * 默认不开启 SecurityContext上下文的解析 加载 ..
 *
 */
public interface LightningSecurityContextRepository extends SecurityContextRepository {

    public static final String TOKEN_HEADER = "Authorization";

    @Override
    default SecurityContext loadContext(HttpRequestResponseHolder requestResponseHolder) {
        return internalLoadContext(requestResponseHolder.getRequest());
    }

    public SecurityContext internalLoadContext(HttpServletRequest request);

}
