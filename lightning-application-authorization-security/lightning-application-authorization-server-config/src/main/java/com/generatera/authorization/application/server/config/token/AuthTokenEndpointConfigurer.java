package com.generatera.authorization.application.server.config.token;

import com.generatera.authorization.application.server.config.ApplicationAuthServerProperties;
import com.generatera.authorization.application.server.config.authentication.LightningAppAuthServerDaoLoginAuthenticationProvider;
import com.generatera.authorization.application.server.config.authorization.store.LightningAuthenticationTokenService;
import com.generatera.authorization.application.server.config.util.AppAuthConfigurerUtils;
import com.generatera.authorization.server.common.configuration.authorization.LightningAuthenticationConverter;
import com.generatera.security.authorization.server.specification.TokenSettingsProvider;
import com.generatera.security.authorization.server.specification.components.authentication.LightningAuthenticationEntryPoint;
import com.generatera.security.authorization.server.specification.components.provider.ProviderSettings;
import com.generatera.security.authorization.server.specification.components.token.LightningToken;
import com.generatera.security.authorization.server.specification.components.token.LightningTokenGenerator;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
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
 * <p>
 * oauth2 copy
 * <p>
 * 1. 支持 access_token 获取
 * 2. 支持token 刷新
 * <p>
 * 3. 默认使用{@link com.generatera.authorization.application.server.config.authentication.DefaultLightningAbstractAuthenticationEntryPoint}进行
 * 登录成功以及登录失败的后置处理,使用统一的响应模型 {@link com.jianyue.lightning.result.Result}
 * <p>
 * <p>
 * 将以下对象作为共享对象:
 * {@link TokenSettingsProvider}
 */
public final class AuthTokenEndpointConfigurer extends AbstractAuthConfigurer {
    private RequestMatcher requestMatcher;
    /**
     * 可以看作 authenticationConverters的替换 ..
     */
    private LightningAuthenticationConverter accessTokenRequestConverter;
    private final List<AuthenticationProvider> authenticationProviders = new LinkedList<>();
    private final List<LightningDaoAuthenticationProvider> daoAuthenticationProviders = new LinkedList<>();
    private AuthenticationSuccessHandler accessTokenResponseHandler;
    private AuthenticationFailureHandler errorResponseHandler;

    private final List<LightningAuthenticationConverter> authenticationConverters = new LinkedList<>();

    public AuthTokenEndpointConfigurer(ObjectPostProcessor<Object> objectPostProcessor) {
        super(objectPostProcessor);
    }

    /**
     * 直接覆盖 acccessTokenRequestConverter ..
     * 相比于覆盖整体逻辑,强烈建议 使用{@link #addAccessTokenRequestConverter}方法
     */
    public AuthTokenEndpointConfigurer accessTokenRequestConverter(LightningAuthenticationConverter accessTokenRequestConverter) {
        this.accessTokenRequestConverter = accessTokenRequestConverter;
        return this;
    }

    /**
     * 增加访问token 请求转换器,和系统默认提供的融合
     */
    public AuthTokenEndpointConfigurer addAccessTokenRequestConverter(LightningAuthenticationConverter... authenticationConverters) {
        this.authenticationConverters.addAll(List.of(authenticationConverters));
        return this;
    }

    /**
     * 增加认证提供器,将会覆盖默认的认证提供器配置 ..
     * 查看{@link #createDefaultAuthenticationProviders(HttpSecurityBuilder)}
     */
    public AuthTokenEndpointConfigurer authenticationProvider(AuthenticationProvider authenticationProvider) {
        Assert.notNull(authenticationProvider, "authenticationProvider cannot be null");
        this.authenticationProviders.add(authenticationProvider);
        return this;
    }

    public AuthTokenEndpointConfigurer authenticationDaoProvider(LightningDaoAuthenticationProvider daoAuthenticationProvider) {
        Assert.notNull(daoAuthenticationProvider, "daoAuthenticationProvider cannot be null");
        this.daoAuthenticationProviders.add(daoAuthenticationProvider);
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
        ProviderSettings providerSettings = AppAuthConfigurerUtils.getProviderSettings(builder).getProviderSettings();
        this.requestMatcher = new AntPathRequestMatcher(providerSettings.getTokenEndpoint(), HttpMethod.POST.name());
        List<AuthenticationProvider> authenticationProviders = !this.authenticationProviders.isEmpty() ? this.authenticationProviders : this.createDefaultAuthenticationProviders(builder);
        authenticationProviders.forEach((authenticationProvider) -> {
            builder.authenticationProvider(this.postProcess(authenticationProvider));
        });
    }

    public <B extends HttpSecurityBuilder<B>> void configure(B builder) {
        AuthenticationManager authenticationManager = builder.getSharedObject(AuthenticationManager.class);
        ProviderSettings providerSettings = AppAuthConfigurerUtils.getProviderSettings(builder).getProviderSettings();
        AuthTokenEndpointFilter tokenEndpointFilter = new AuthTokenEndpointFilter(authenticationManager, providerSettings.getTokenEndpoint());

        ApplicationAuthServerProperties authServerProperties = builder.getSharedObject(ApplicationAuthServerProperties.class);
        // 本质上它将
        LightningAppAuthServerDaoLoginAuthenticationProvider authenticationProvider = new LightningAppAuthServerDaoLoginAuthenticationProvider(
                // 将会获取一个 生成 token的 provider ...
                AppAuthConfigurerUtils.getAppAuthServerForTokenAuthenticationProvider(builder),
                // 获取dao 认证提供器 ..
                // 将两种注册方式的提供器进行融合 ...
                AppAuthConfigurerUtils.getDaoAuthenticationProvider(builder, daoAuthenticationProviders),
                authServerProperties.isSeparation());

        // 实现 用户登录 认证 ..
        builder.authenticationProvider(authenticationProvider);

        // 如果提供,则使用
        if (!authenticationConverters.isEmpty()) {
            AnnotationAwareOrderComparator awareOrderComparator = new AnnotationAwareOrderComparator();
            authenticationConverters.sort(awareOrderComparator);
            tokenEndpointFilter.setAuthenticationConverter(
                    new DelegatingAuthenticationConverter(
                            List.of(
                                    new AuthLoginRequestAuthenticationConverter(authenticationConverters),
                                    new AuthRefreshTokenAuthenticationConverter()
                            )
                    )
            );
        }

        // 否则可能全部被覆盖了 ..
        // 访问token 请求转换器
        if (this.accessTokenRequestConverter != null) {
            tokenEndpointFilter.setAuthenticationConverter(this.accessTokenRequestConverter);
        }

        // ------------------------ 覆盖 ------------------------------------
        LightningAuthenticationEntryPoint entryPoint = AppAuthConfigurerUtils.getAuthenticationEntryPoint(builder);

        /**
         * 访问token响应处理器
         */
        if (this.accessTokenResponseHandler != null) {
            tokenEndpointFilter.setAuthenticationSuccessHandler(this.accessTokenResponseHandler);
        } else {
            tokenEndpointFilter.setAuthenticationSuccessHandler(entryPoint);
        }

        /**
         * 错误响应处理器
         */
        if (this.errorResponseHandler != null) {
            tokenEndpointFilter.setAuthenticationFailureHandler(this.errorResponseHandler);
        } else {
            tokenEndpointFilter.setAuthenticationFailureHandler(entryPoint);
        }

        builder.addFilterAfter(this.postProcess(tokenEndpointFilter), FilterSecurityInterceptor.class);
    }

    public RequestMatcher getRequestMatcher() {
        return this.requestMatcher;
    }

    /**
     * 创建默认的 认证提供器 ...
     */
    private <B extends HttpSecurityBuilder<B>> List<AuthenticationProvider> createDefaultAuthenticationProviders(B builder) {
        List<AuthenticationProvider> authenticationProviders = new ArrayList<>();

        LightningAuthenticationTokenService authorizationService = AppAuthConfigurerUtils.getAuthorizationService(builder);
        LightningTokenGenerator<? extends LightningToken> tokenGenerator = AppAuthConfigurerUtils.getTokenGenerator(builder);
        TokenSettingsProvider tokenSettingProvider = AppAuthConfigurerUtils.getTokenSettingProvider(builder);
        LightningUserDetailsProvider userDetailsService = AppAuthConfigurerUtils.getLightningUserDetailsProvider(builder);


        AuthRefreshTokenAuthenticationProvider refreshTokenAuthenticationProvider = new AuthRefreshTokenAuthenticationProvider(
                authorizationService,
                tokenGenerator,
                tokenSettingProvider,
                userDetailsService
        );
        authenticationProviders.add(AppAuthConfigurerUtils.getAppAuthServerForTokenAuthenticationProvider(builder));
        authenticationProviders.add(refreshTokenAuthenticationProvider);
        return authenticationProviders;
    }


}
