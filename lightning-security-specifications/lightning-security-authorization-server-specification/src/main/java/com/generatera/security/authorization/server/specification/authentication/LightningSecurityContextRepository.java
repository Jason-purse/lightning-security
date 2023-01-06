package com.generatera.security.authorization.server.specification.authentication;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.SecurityContextRepository;

import javax.servlet.http.HttpServletRequest;

public interface LightningSecurityContextRepository extends SecurityContextRepository {

    public static final String TOKEN_HEADER = "Authorization";

    @Override
    default SecurityContext loadContext(HttpRequestResponseHolder requestResponseHolder) {
        return internalLoadContext(requestResponseHolder.getRequest());
    }

    public SecurityContext internalLoadContext(HttpServletRequest request);

}
