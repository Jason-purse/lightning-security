package com.generatera.authorization.application.server.config.token;

import com.generatera.authorization.application.server.config.util.AuthConfigurerUtils;
import com.generatera.security.authorization.server.specification.ProviderExtUtils;
import com.generatera.security.authorization.server.specification.components.provider.ProviderSettings;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
/**
 * @author FLJ
 * @date 2023/1/28
 * @time 13:36
 * @Description 认证 token 撤销端点 ..
 */
public final class AuthTokenRevocationEndpointConfigurer extends AbstractAuthConfigurer {
    private RequestMatcher requestMatcher;
    private AuthenticationConverter revocationRequestConverter;
    private final List<AuthenticationProvider> authenticationProviders = new LinkedList<>();
    private AuthenticationSuccessHandler revocationResponseHandler;
    private AuthenticationFailureHandler errorResponseHandler;

    public AuthTokenRevocationEndpointConfigurer(ObjectPostProcessor<Object> objectPostProcessor) {
        super(objectPostProcessor);
    }

    public AuthTokenRevocationEndpointConfigurer revocationRequestConverter(AuthenticationConverter revocationRequestConverter) {
        this.revocationRequestConverter = revocationRequestConverter;
        return this;
    }

    public AuthTokenRevocationEndpointConfigurer authenticationProvider(AuthenticationProvider authenticationProvider) {
        Assert.notNull(authenticationProvider, "authenticationProvider cannot be null");
        this.authenticationProviders.add(authenticationProvider);
        return this;
    }

    public AuthTokenRevocationEndpointConfigurer revocationResponseHandler(AuthenticationSuccessHandler revocationResponseHandler) {
        this.revocationResponseHandler = revocationResponseHandler;
        return this;
    }

    public AuthTokenRevocationEndpointConfigurer errorResponseHandler(AuthenticationFailureHandler errorResponseHandler) {
        this.errorResponseHandler = errorResponseHandler;
        return this;
    }

    public <B extends HttpSecurityBuilder<B>> void init(B builder) {
        ProviderSettings providerSettings = ProviderExtUtils.getProviderSettings(builder).getProviderSettings();
        this.requestMatcher = new AntPathRequestMatcher(providerSettings.getTokenRevocationEndpoint(), HttpMethod.POST.name());
        List<AuthenticationProvider> authenticationProviders = !this.authenticationProviders.isEmpty() ? this.authenticationProviders : this.createDefaultAuthenticationProviders(builder);
        authenticationProviders.forEach((authenticationProvider) -> {
            builder.authenticationProvider((AuthenticationProvider)this.postProcess(authenticationProvider));
        });
    }

    public <B extends HttpSecurityBuilder<B>> void configure(B builder) {
        AuthenticationManager authenticationManager = (AuthenticationManager)builder.getSharedObject(AuthenticationManager.class);
        ProviderSettings providerSettings = ProviderExtUtils.getProviderSettings(builder).getProviderSettings();
        AuthTokenRevocationEndpointFilter revocationEndpointFilter = new AuthTokenRevocationEndpointFilter(authenticationManager, providerSettings.getTokenRevocationEndpoint());
        if (this.revocationRequestConverter != null) {
            revocationEndpointFilter.setAuthenticationConverter(this.revocationRequestConverter);
        }

        if (this.revocationResponseHandler != null) {
            revocationEndpointFilter.setAuthenticationSuccessHandler(this.revocationResponseHandler);
        }

        if (this.errorResponseHandler != null) {
            revocationEndpointFilter.setAuthenticationFailureHandler(this.errorResponseHandler);
        }

        builder.addFilterAfter(this.postProcess(revocationEndpointFilter), FilterSecurityInterceptor.class);
    }

    public RequestMatcher getRequestMatcher() {
        return this.requestMatcher;
    }

    private <B extends HttpSecurityBuilder<B>> List<AuthenticationProvider> createDefaultAuthenticationProviders(B builder) {
        List<AuthenticationProvider> authenticationProviders = new ArrayList();
        AuthTokenRevocationAuthenticationProvider tokenRevocationAuthenticationProvider = new AuthTokenRevocationAuthenticationProvider(AuthConfigurerUtils.getAuthorizationService(builder));
        authenticationProviders.add(tokenRevocationAuthenticationProvider);
        return authenticationProviders;
    }
}
