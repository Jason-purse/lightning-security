package com.generatera.authorization.application.server.config.token;

import com.generatera.security.authorization.server.specification.ProviderExtUtils;
import com.generatera.security.authorization.server.specification.ProviderSettingsProvider;
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
 * @time 16:43
 * @Description token省查配置器 ..
 */
public final class AuthTokenIntrospectionEndpointConfigurer extends AbstractAuthConfigurer {
    private RequestMatcher requestMatcher;
    private AuthenticationConverter introspectionRequestConverter;
    private final List<AuthenticationProvider> authenticationProviders = new LinkedList<>();
    private AuthenticationSuccessHandler introspectionResponseHandler;
    private AuthenticationFailureHandler errorResponseHandler;

    public AuthTokenIntrospectionEndpointConfigurer(ObjectPostProcessor<Object> objectPostProcessor) {
        super(objectPostProcessor);
    }

    public AuthTokenIntrospectionEndpointConfigurer introspectionRequestConverter(AuthenticationConverter introspectionRequestConverter) {
        this.introspectionRequestConverter = introspectionRequestConverter;
        return this;
    }

    public AuthTokenIntrospectionEndpointConfigurer authenticationProvider(AuthenticationProvider authenticationProvider) {
        Assert.notNull(authenticationProvider, "authenticationProvider cannot be null");
        this.authenticationProviders.add(authenticationProvider);
        return this;
    }

    public AuthTokenIntrospectionEndpointConfigurer introspectionResponseHandler(AuthenticationSuccessHandler introspectionResponseHandler) {
        this.introspectionResponseHandler = introspectionResponseHandler;
        return this;
    }

    public AuthTokenIntrospectionEndpointConfigurer errorResponseHandler(AuthenticationFailureHandler errorResponseHandler) {
        this.errorResponseHandler = errorResponseHandler;
        return this;
    }

    public <B extends HttpSecurityBuilder<B>> void init(B builder) {
        ProviderSettingsProvider providerSettings = ProviderExtUtils.getProviderSettings(builder);
        this.requestMatcher = new AntPathRequestMatcher(providerSettings.getProviderSettings().getTokenIntrospectionEndpoint(), HttpMethod.POST.name());
        List<AuthenticationProvider> authenticationProviders = !this.authenticationProviders.isEmpty() ? this.authenticationProviders : this.createDefaultAuthenticationProviders(builder);
        authenticationProviders.forEach((authenticationProvider) -> {
            builder.authenticationProvider(this.postProcess(authenticationProvider));
        });
    }

    public <B extends HttpSecurityBuilder<B>> void configure(B builder) {
        AuthenticationManager authenticationManager = builder.getSharedObject(AuthenticationManager.class);
        ProviderSettingsProvider providerSettings = ProviderExtUtils.getProviderSettings(builder);
        TokenIntrospectFilter introspectionEndpointFilter = new TokenIntrospectFilter(authenticationManager, providerSettings.getProviderSettings().getTokenIntrospectionEndpoint());
        if (this.introspectionRequestConverter != null) {
            introspectionEndpointFilter.setAuthenticationConverter(this.introspectionRequestConverter);
        }

        if (this.introspectionResponseHandler != null) {
            introspectionEndpointFilter.setAuthenticationSuccessHandler(this.introspectionResponseHandler);
        }

        if (this.errorResponseHandler != null) {
            introspectionEndpointFilter.setAuthenticationFailureHandler(this.errorResponseHandler);
        }

        builder.addFilterAfter(this.postProcess(introspectionEndpointFilter), FilterSecurityInterceptor.class);
    }

    public RequestMatcher getRequestMatcher() {
        return this.requestMatcher;
    }

    private <B extends HttpSecurityBuilder<B>> List<AuthenticationProvider> createDefaultAuthenticationProviders(B builder) {
        List<AuthenticationProvider> authenticationProviders = new ArrayList<>();
        AuthTokenIntrospectionAuthenticationProvider tokenIntrospectionAuthenticationProvider =
                new AuthTokenIntrospectionAuthenticationProvider(AuthConfigurerUtils.getAuthorizationService(builder),
                AuthConfigurerUtils.getTokenSettingProvider(builder));
        authenticationProviders.add(tokenIntrospectionAuthenticationProvider);
        return authenticationProviders;
    }
}