package com.generatera.authorization.server.common.configuration.token;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.SecurityContextRepository;

import javax.servlet.http.HttpServletRequest;

/**
 * @author FLJ
 * @date 2023/1/5
 * @time 9:46
 * @Description 基于 spring security的 Lightning SecurityContext Repository
 */
public interface LightningSecurityContextRepository extends SecurityContextRepository {

    public static final String TOKEN_HEADER = "Authorization";


    @Override
    default SecurityContext loadContext(HttpRequestResponseHolder requestResponseHolder) {
        return internalLoadContext(requestResponseHolder.getRequest());
    }


    SecurityContext internalLoadContext(HttpServletRequest request);
}
