package com.generatera.authorization.application.server.config;

import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.access.RequestMatcherDelegatingAccessDeniedHandler;
import org.springframework.security.web.authentication.DelegatingAuthenticationEntryPoint;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.LinkedHashMap;

public class MyExceptionHandingConfigurer <H extends HttpSecurityBuilder<H>> extends AbstractHttpConfigurer<MyExceptionHandingConfigurer<H>, H> {
    private AuthenticationEntryPoint authenticationEntryPoint;
    private AccessDeniedHandler accessDeniedHandler;
    private LinkedHashMap<RequestMatcher, AuthenticationEntryPoint> defaultEntryPointMappings = new LinkedHashMap();
    private LinkedHashMap<RequestMatcher, AccessDeniedHandler> defaultDeniedHandlerMappings = new LinkedHashMap();

    public MyExceptionHandingConfigurer() {
    }

    public MyExceptionHandingConfigurer<H> accessDeniedPage(String accessDeniedUrl) {
        AccessDeniedHandlerImpl accessDeniedHandler = new AccessDeniedHandlerImpl();
        accessDeniedHandler.setErrorPage(accessDeniedUrl);
        return this.accessDeniedHandler(accessDeniedHandler);
    }

    public MyExceptionHandingConfigurer<H> accessDeniedHandler(AccessDeniedHandler accessDeniedHandler) {
        this.accessDeniedHandler = accessDeniedHandler;
        return this;
    }

    public MyExceptionHandingConfigurer<H> defaultAccessDeniedHandlerFor(AccessDeniedHandler deniedHandler, RequestMatcher preferredMatcher) {
        this.defaultDeniedHandlerMappings.put(preferredMatcher, deniedHandler);
        return this;
    }

    public MyExceptionHandingConfigurer<H> authenticationEntryPoint(AuthenticationEntryPoint authenticationEntryPoint) {
        this.authenticationEntryPoint = authenticationEntryPoint;
        return this;
    }

    public MyExceptionHandingConfigurer<H> defaultAuthenticationEntryPointFor(AuthenticationEntryPoint entryPoint, RequestMatcher preferredMatcher) {
        this.defaultEntryPointMappings.put(preferredMatcher, entryPoint);
        return this;
    }

    AuthenticationEntryPoint getAuthenticationEntryPoint() {
        return this.authenticationEntryPoint;
    }

    AccessDeniedHandler getAccessDeniedHandler() {
        return this.accessDeniedHandler;
    }

    public void configure(H http) {
        AuthenticationEntryPoint entryPoint = this.getAuthenticationEntryPoint(http);
        ExceptionTranslationFilter exceptionTranslationFilter = new ExceptionTranslationFilter(entryPoint, this.getRequestCache(http));
        AccessDeniedHandler deniedHandler = this.getAccessDeniedHandler(http);
        exceptionTranslationFilter.setAccessDeniedHandler(deniedHandler);
        exceptionTranslationFilter = (ExceptionTranslationFilter)this.postProcess(exceptionTranslationFilter);
        http.addFilter(exceptionTranslationFilter);
    }

    AccessDeniedHandler getAccessDeniedHandler(H http) {
        AccessDeniedHandler deniedHandler = this.accessDeniedHandler;
        if (deniedHandler == null) {
            deniedHandler = this.createDefaultDeniedHandler(http);
        }

        return deniedHandler;
    }

    AuthenticationEntryPoint getAuthenticationEntryPoint(H http) {
        AuthenticationEntryPoint entryPoint = this.authenticationEntryPoint;
        if (entryPoint == null) {
            entryPoint = this.createDefaultEntryPoint(http);
        }

        return entryPoint;
    }

    private AccessDeniedHandler createDefaultDeniedHandler(H http) {
        if (this.defaultDeniedHandlerMappings.isEmpty()) {
            return new AccessDeniedHandlerImpl();
        } else {
            return (AccessDeniedHandler)(this.defaultDeniedHandlerMappings.size() == 1 ? (AccessDeniedHandler)this.defaultDeniedHandlerMappings.values().iterator().next() : new RequestMatcherDelegatingAccessDeniedHandler(this.defaultDeniedHandlerMappings, new AccessDeniedHandlerImpl()));
        }
    }

    private AuthenticationEntryPoint createDefaultEntryPoint(H http) {
        if (this.defaultEntryPointMappings.isEmpty()) {
            return new Http403ForbiddenEntryPoint();
        } else if (this.defaultEntryPointMappings.size() == 1) {
            return (AuthenticationEntryPoint)this.defaultEntryPointMappings.values().iterator().next();
        } else {
            DelegatingAuthenticationEntryPoint entryPoint = new DelegatingAuthenticationEntryPoint(this.defaultEntryPointMappings);
            entryPoint.setDefaultEntryPoint((AuthenticationEntryPoint)this.defaultEntryPointMappings.values().iterator().next());
            return entryPoint;
        }
    }

    private RequestCache getRequestCache(H http) {
        RequestCache result = (RequestCache)http.getSharedObject(RequestCache.class);
        return (RequestCache)(result != null ? result : new HttpSessionRequestCache());
    }
}
