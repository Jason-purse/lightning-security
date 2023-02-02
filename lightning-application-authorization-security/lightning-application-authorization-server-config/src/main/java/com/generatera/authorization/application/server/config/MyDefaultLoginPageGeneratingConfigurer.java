package com.generatera.authorization.application.server.config;

import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.ui.DefaultLoginPageGeneratingFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Map;

public class MyDefaultLoginPageGeneratingConfigurer<H extends HttpSecurityBuilder<H>> extends AbstractHttpConfigurer<MyDefaultLoginPageGeneratingConfigurer<H>, H> {

    private DefaultLoginPageGeneratingFilter loginPageGeneratingFilter = new DefaultLoginPageGeneratingFilter();
    private MyDefaultLogoutPageGeneratingFilter logoutPageGeneratingFilter = new MyDefaultLogoutPageGeneratingFilter();

    public MyDefaultLoginPageGeneratingConfigurer() {
    }

    public MyDefaultLoginPageGeneratingConfigurer<H> setLoginPageGeneratingFilter(DefaultLoginPageGeneratingFilter loginPageGeneratingFilter) {
        this.loginPageGeneratingFilter = loginPageGeneratingFilter;
        return this;
    }

    public MyDefaultLoginPageGeneratingConfigurer<H> setLogoutPageGeneratingFilter(MyDefaultLogoutPageGeneratingFilter logoutPageGeneratingFilter) {
        this.logoutPageGeneratingFilter = logoutPageGeneratingFilter;
        return this;
    }

    public MyDefaultLoginPageGeneratingConfigurer<H> setLogoutPageRequestMatcher(RequestMatcher requestMatcher) {
        this.logoutPageGeneratingFilter.setMatcher(requestMatcher);
        return this;
    }

    public void init(H http) {
        this.loginPageGeneratingFilter.setResolveHiddenInputs(this::hiddenInputs);
        this.logoutPageGeneratingFilter.setResolveHiddenInputs(this::hiddenInputs);
        http.setSharedObject(DefaultLoginPageGeneratingFilter.class, this.loginPageGeneratingFilter);
    }

    private Map<String, String> hiddenInputs(HttpServletRequest request) {
        CsrfToken token = (CsrfToken)request.getAttribute(CsrfToken.class.getName());
        return token != null ? Collections.singletonMap(token.getParameterName(), token.getToken()) : Collections.emptyMap();
    }

    public void configure(H http) {
        AuthenticationEntryPoint authenticationEntryPoint = null;
        MyExceptionHandingConfigurer<?> exceptionConf = (MyExceptionHandingConfigurer)http.getConfigurer(MyExceptionHandingConfigurer.class);
        if (exceptionConf != null) {
            authenticationEntryPoint = exceptionConf.getAuthenticationEntryPoint();
        }

        if (this.loginPageGeneratingFilter.isEnabled() && authenticationEntryPoint == null) {
            this.loginPageGeneratingFilter = this.postProcess(this.loginPageGeneratingFilter);
            http.addFilter(this.loginPageGeneratingFilter);
            LogoutConfigurer<H> logoutConfigurer = (LogoutConfigurer)http.getConfigurer(LogoutConfigurer.class);
            if (logoutConfigurer != null) {
                http.addFilter(this.logoutPageGeneratingFilter);
            }
        }

    }
}
