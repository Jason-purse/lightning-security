package com.generatera.oauth2.resource.server.config.token;

import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;

import javax.servlet.http.HttpServletRequest;

public class LightningDefaultBearerTokenResolver implements BearerTokenResolver, LightningAuthenticationTokenResolver{

    private final BearerTokenResolver resolver = new DefaultBearerTokenResolver();
    @Override
    public String doResolve(HttpServletRequest request) {
        return resolve(request);
    }

    @Override
    public String resolve(HttpServletRequest request) {
        return resolver.resolve(request);
    }
}
