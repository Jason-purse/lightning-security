package com.generatera.oauth2.resource.server.config.token;

import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;

import javax.servlet.http.HttpServletRequest;

public class LightningDefaultBearerTokenResolver implements BearerTokenResolver {

    private final DefaultBearerTokenResolver resolver = new DefaultBearerTokenResolver();

    public LightningDefaultBearerTokenResolver(String tokenHeaderName) {
        this.resolver.setBearerTokenHeaderName(tokenHeaderName);
    }

    @Override
    public String resolve(HttpServletRequest request) {
        return resolver.resolve(request);
    }
}
