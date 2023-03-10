package com.generatera.authorization.application.server.config.token;

import com.generatera.authorization.application.server.config.ApplicationAuthServerProperties;
import com.generatera.authorization.application.server.config.authentication.LightningAppAuthServerDaoLoginAuthenticationProvider;
import com.generatera.authorization.application.server.config.authorization.store.LightningAuthenticationTokenService;
import com.generatera.authorization.application.server.config.util.AppAuthConfigurerUtils;
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
 * @Description ????????????Token ...
 * <p>
 * oauth2 copy
 * <p>
 * 1. ?????? access_token ??????
 * 2. ??????token ??????
 * <p>
 * 3. ????????????{@link com.generatera.authorization.application.server.config.authentication.DefaultLightningAbstractAuthenticationEntryPoint}??????
 * ?????????????????????????????????????????????,??????????????????????????? {@link com.jianyue.lightning.result.Result}
 * <p>
 * <p>
 * ?????????????????????????????????:
 * {@link TokenSettingsProvider}
 */
public final class AuthTokenEndpointConfigurer extends AbstractAuthConfigurer {
    private RequestMatcher requestMatcher;
    private AuthenticationConverter accessTokenRequestConverter;
    private final List<AuthenticationProvider> authenticationProviders = new LinkedList<>();
    private final List<LightningDaoAuthenticationProvider> daoAuthenticationProviders = new LinkedList<>();
    private AuthenticationSuccessHandler accessTokenResponseHandler;
    private AuthenticationFailureHandler errorResponseHandler;

    private final List<AuthenticationConverter> authenticationConverters = new LinkedList<>();

    public AuthTokenEndpointConfigurer(ObjectPostProcessor<Object> objectPostProcessor) {
        super(objectPostProcessor);
    }

    /**
     * ???????????? acccessTokenRequestConverter ..
     * ???????????????????????????,???????????? ??????{@link #addAccessTokenRequestConverter}??????
     */
    public AuthTokenEndpointConfigurer accessTokenRequestConverter(AuthenticationConverter accessTokenRequestConverter) {
        this.accessTokenRequestConverter = accessTokenRequestConverter;
        return this;
    }

    /**
     * ????????????token ???????????????,??????????????????????????????
     */
    public AuthTokenEndpointConfigurer addAccessTokenRequestConverter(AuthenticationConverter... authenticationConverters) {
        this.authenticationConverters.addAll(List.of(authenticationConverters));
        return this;
    }

    /**
     * ?????????????????????,?????????????????????????????????????????? ..
     * ??????{@link #createDefaultAuthenticationProviders(HttpSecurityBuilder)}
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
        // ???????????????
        LightningAppAuthServerDaoLoginAuthenticationProvider authenticationProvider = new LightningAppAuthServerDaoLoginAuthenticationProvider(
                // ?????????????????? ?????? token??? provider ...
                AppAuthConfigurerUtils.getAppAuthServerForTokenAuthenticationProvider(builder),
                // ??????dao ??????????????? ..
                // ????????????????????????????????????????????? ...
                AppAuthConfigurerUtils.getDaoAuthenticationProvider(builder, daoAuthenticationProviders),
                authServerProperties.isSeparation());

        // ?????? ???????????? ?????? ..
        builder.authenticationProvider(authenticationProvider);

        // ????????????,?????????
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

        // ?????????????????????????????? ..
        // ??????token ???????????????
        if (this.accessTokenRequestConverter != null) {
            tokenEndpointFilter.setAuthenticationConverter(this.accessTokenRequestConverter);
        }

        // ------------------------ ?????? ------------------------------------
        LightningAuthenticationEntryPoint entryPoint = AppAuthConfigurerUtils.getAuthenticationEntryPoint(builder);

        /**
         * ??????token???????????????
         */
        if (this.accessTokenResponseHandler != null) {
            tokenEndpointFilter.setAuthenticationSuccessHandler(this.accessTokenResponseHandler);
        } else {
            tokenEndpointFilter.setAuthenticationSuccessHandler(entryPoint);
        }

        /**
         * ?????????????????????
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
     * ??????????????? ??????????????? ...
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
