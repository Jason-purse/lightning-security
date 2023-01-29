package com.generatera.authorization.application.server.config.token;

import com.generatera.authorization.server.common.configuration.authorization.store.LightningAuthenticationTokenService;
import com.generatera.security.authorization.server.specification.ProviderExtUtils;
import com.generatera.security.authorization.server.specification.TokenSettingsProvider;
import com.generatera.security.authorization.server.specification.components.provider.ProviderSettings;
import com.generatera.security.authorization.server.specification.components.token.LightningToken;
import com.generatera.security.authorization.server.specification.components.token.LightningTokenGenerator;
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
 * @time 15:56
 * @Description 负责刷新Token ...
 *
 * oauth2 copy
 */
public final class AuthTokenEndpointConfigurer extends AbstractAuthConfigurer {
    private RequestMatcher requestMatcher;
    private AuthenticationConverter accessTokenRequestConverter;
    private final List<AuthenticationProvider> authenticationProviders = new LinkedList<>();
    private AuthenticationSuccessHandler accessTokenResponseHandler;
    private AuthenticationFailureHandler errorResponseHandler;

    public AuthTokenEndpointConfigurer(ObjectPostProcessor<Object> objectPostProcessor) {
        super(objectPostProcessor);
    }

    public AuthTokenEndpointConfigurer accessTokenRequestConverter(AuthenticationConverter accessTokenRequestConverter) {
        this.accessTokenRequestConverter = accessTokenRequestConverter;
        return this;
    }

    public AuthTokenEndpointConfigurer authenticationProvider(AuthenticationProvider authenticationProvider) {
        Assert.notNull(authenticationProvider, "authenticationProvider cannot be null");
        this.authenticationProviders.add(authenticationProvider);
        return this;
    }

    public AuthTokenEndpointConfigurer accessTokenResponseHandler(AuthenticationSuccessHandler accessTokenResponseHandler) {
        this.accessTokenResponseHandler = accessTokenResponseHandler;
        return this;
    }

    public AuthTokenEndpointConfigurer errorResponseHandler(AuthenticationFailureHandler errorResponseHandler) {
        this.errorResponseHandler = errorResponseHandler;
        return this;
    }

    public <B extends HttpSecurityBuilder<B>> void init(B builder) {
        ProviderSettings providerSettings = ProviderExtUtils.getProviderSettings(builder).getProviderSettings();
        this.requestMatcher = new AntPathRequestMatcher(providerSettings.getTokenEndpoint(), HttpMethod.POST.name());
        List<AuthenticationProvider> authenticationProviders = !this.authenticationProviders.isEmpty() ? this.authenticationProviders : this.createDefaultAuthenticationProviders(builder);
        authenticationProviders.forEach((authenticationProvider) -> {
            builder.authenticationProvider(this.postProcess(authenticationProvider));
        });
    }

    public <B extends HttpSecurityBuilder<B>> void configure(B builder) {
        AuthenticationManager authenticationManager = builder.getSharedObject(AuthenticationManager.class);
        ProviderSettings providerSettings = ProviderExtUtils.getProviderSettings(builder).getProviderSettings();

        AppAuthServerForTokenAuthenticationProvider tokenAuthenticationProvider = builder.getSharedObject(AppAuthServerForTokenAuthenticationProvider.class);
        // 默认配置,认证提供器需要提前创建 .. 填坑
        if(tokenAuthenticationProvider instanceof AuthAccessTokenAuthenticationProvider accessTokenAuthenticationProvider) {
            accessTokenAuthenticationProvider.setAuthenticationManager(builder.getSharedObject(AuthenticationManager.class));
        }

        AuthTokenEndpointFilter tokenEndpointFilter = new AuthTokenEndpointFilter(authenticationManager, providerSettings.getTokenEndpoint());
        // 访问token 请求转换器
        if (this.accessTokenRequestConverter != null) {
            tokenEndpointFilter.setAuthenticationConverter(this.accessTokenRequestConverter);
        }

        /**
         * 访问token响应处理器
         */
        if (this.accessTokenResponseHandler != null) {
            tokenEndpointFilter.setAuthenticationSuccessHandler(this.accessTokenResponseHandler);
        }

        /**
         * 错误响应处理器
         */
        if (this.errorResponseHandler != null) {
            tokenEndpointFilter.setAuthenticationFailureHandler(this.errorResponseHandler);
        }

        builder.addFilterAfter(this.postProcess(tokenEndpointFilter), FilterSecurityInterceptor.class);
    }

    public RequestMatcher getRequestMatcher() {
        return this.requestMatcher;
    }

    private <B extends HttpSecurityBuilder<B>> List<AuthenticationProvider> createDefaultAuthenticationProviders(B builder) {
        List<AuthenticationProvider> authenticationProviders = new ArrayList<>();

        LightningAuthenticationTokenService authorizationService = AuthConfigurerUtils.getAuthorizationService(builder);
        LightningTokenGenerator<? extends LightningToken> tokenGenerator = AuthConfigurerUtils.getTokenGenerator(builder);
        TokenSettingsProvider tokenSettingProvider = AuthConfigurerUtils.getTokenSettingProvider(builder);

        AuthAccessTokenAuthenticationProvider accessTokenAuthenticationProvider = new AuthAccessTokenAuthenticationProvider(
                authorizationService,
                tokenGenerator,
                tokenSettingProvider
        );

        // 为了获取 AppAuthServerForTokenAuthenticationProvider
        builder.setSharedObject(AppAuthServerForTokenAuthenticationProvider.class,accessTokenAuthenticationProvider);

        AuthRefreshTokenAuthenticationProvider refreshTokenAuthenticationProvider = new AuthRefreshTokenAuthenticationProvider(
                authorizationService,
                tokenGenerator,
                tokenSettingProvider
        );
        authenticationProviders.add(accessTokenAuthenticationProvider);
        authenticationProviders.add(refreshTokenAuthenticationProvider);
        return authenticationProviders;
    }
}
