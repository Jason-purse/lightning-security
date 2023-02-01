package com.generatera.authorization.application.server.config;

import com.generatera.authorization.application.server.config.authorization.store.LightningAuthenticationTokenService;
import com.generatera.authorization.application.server.config.token.*;
import com.generatera.authorization.application.server.config.util.AppAuthConfigurerUtils;
import com.generatera.authorization.server.common.configuration.provider.AuthorizationServerNimbusJwkSetEndpointFilter;
import com.generatera.authorization.server.common.configuration.provider.metadata.AuthorizationProviderContextFilter;
import com.generatera.authorization.server.common.configuration.provider.metadata.AuthorizationServerMetadataEndpointFilter;
import com.generatera.security.authorization.server.specification.AuthServerProvider;
import com.generatera.security.authorization.server.specification.ProviderExtUtils;
import com.generatera.security.authorization.server.specification.components.provider.ProviderSettings;
import com.generatera.security.authorization.server.specification.components.token.LightningToken;
import com.generatera.security.authorization.server.specification.components.token.LightningTokenGenerator;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author FLJ
 * @date 2023/1/28
 * @time 13:15
 * @Description app auth server configurer
 * <p>
 * 主要包含几大内容,
 * - token introspect / token revoke
 * - provider context
 * - authentication token service
 * - jwk set
 */
public class ApplicationAuthServerConfigurer<B extends HttpSecurityBuilder<B>> extends AbstractHttpConfigurer<ApplicationAuthServerConfigurer<B>, B> {

    public static final String AUTHORIZATION_SERVER_METADATA_ENDPOINT_URL = "/.well-known/oauth-authorization-server";

    private final Map<Class<? extends AbstractAuthConfigurer>, AbstractAuthConfigurer> configurers = this.createConfigurers();
    private RequestMatcher jwkSetEndpointMatcher;
    private RequestMatcher authorizationServerMetadataEndpointMatcher;
    private final RequestMatcher endpointsMatcher = (request) -> {
        return this.getRequestMatcher(AuthTokenEndpointConfigurer.class).matches(request)
                || this.getRequestMatcher(AuthTokenRevocationEndpointConfigurer.class).matches(request)
                || this.getRequestMatcher(AuthTokenIntrospectionEndpointConfigurer.class).matches(request)
                || this.getRequestMatcher(OpenConnectAuthServerMetadataConfigurer.class).matches(request)
                || this.jwkSetEndpointMatcher.matches(request)
                || this.authorizationServerMetadataEndpointMatcher.matches(request);
    };


    public ApplicationAuthServerConfigurer() {
    }


    public ApplicationAuthServerConfigurer<B> authorizationService(LightningAuthenticationTokenService authorizationService) {
        Assert.notNull(authorizationService, "authorizationService cannot be null");
        this.getBuilder().setSharedObject(LightningAuthenticationTokenService.class, authorizationService);
        return this;
    }


    public ApplicationAuthServerConfigurer<B> providerSettings(ProviderSettings providerSettings) {
        Assert.notNull(providerSettings, "providerSettings cannot be null");
        this.getBuilder().setSharedObject(ProviderSettings.class, providerSettings);
        return this;
    }

    public ApplicationAuthServerConfigurer<B> tokenGenerator(LightningTokenGenerator<? extends LightningToken> tokenGenerator) {
        Assert.notNull(tokenGenerator, "tokenGenerator cannot be null");
        this.getBuilder().setSharedObject(LightningTokenGenerator.class, tokenGenerator);
        return this;
    }

    public ApplicationAuthServerConfigurer<B> tokenIntrospectionEndpoint(Customizer<AuthTokenIntrospectionEndpointConfigurer> tokenIntrospectionEndpointCustomizer) {
        AuthTokenIntrospectionEndpointConfigurer configurer = this.getConfigurer(AuthTokenIntrospectionEndpointConfigurer.class);
        if (configurer == null) {
            AuthTokenIntrospectionEndpointConfigurer endpointConfigurer = new AuthTokenIntrospectionEndpointConfigurer(this::postProcess);
            configurers.put(AuthTokenIntrospectionEndpointConfigurer.class, endpointConfigurer);
            configurer = endpointConfigurer;
        }
        tokenIntrospectionEndpointCustomizer.customize(configurer);
        return this;
    }

    public ApplicationAuthServerConfigurer<B> tokenRevocationEndpoint(Customizer<AuthTokenRevocationEndpointConfigurer> tokenRevocationEndpointCustomizer) {
        tokenRevocationEndpointCustomizer.customize(this.getConfigurer(AuthTokenRevocationEndpointConfigurer.class));
        return this;
    }

    public ApplicationAuthServerConfigurer<B> tokenEndpoint(Customizer<AuthTokenEndpointConfigurer> tokenEndpointConfigurerCustomizer) {
        tokenEndpointConfigurerCustomizer.customize(this.getConfigurer(AuthTokenEndpointConfigurer.class));
        return this;
    }

    public ApplicationAuthServerConfigurer<B> oidcProviderMeta(Customizer<OpenConnectAuthServerMetadataConfigurer> openConnectAuthServerMetadataConfigurerCustomizer) {
        openConnectAuthServerMetadataConfigurerCustomizer.customize( this.getConfigurer(OpenConnectAuthServerMetadataConfigurer.class));
        return this;
    }


    public RequestMatcher getEndpointsMatcher() {
        return this.endpointsMatcher;
    }

    @SuppressWarnings("unchecked")
    public void init(B builder) {
        AuthServerProvider providerSettings = AppAuthConfigurerUtils.getProviderSettings(builder);
        validateProviderSettings(providerSettings.getProviderSettings());
        this.initEndpointMatchers(providerSettings.getProviderSettings());
        this.configurers.values().forEach((configurer) -> {
            configurer.init(builder);
        });


        // 异常处理机制 ..
        // 如果没有,则直接给出401 ...
        ExceptionHandlingConfigurer<B> exceptionHandling = (ExceptionHandlingConfigurer<B>) builder.getConfigurer(ExceptionHandlingConfigurer.class);
        if (exceptionHandling != null) {

            AuthTokenIntrospectionEndpointConfigurer configurer = getConfigurer(AuthTokenIntrospectionEndpointConfigurer.class);

            List<RequestMatcher> matchers = new LinkedList<>();
            matchers.add(this.getRequestMatcher(AuthTokenEndpointConfigurer.class));
            matchers.add(this.getRequestMatcher(AuthTokenRevocationEndpointConfigurer.class));
            matchers.add(this.getRequestMatcher(AuthTokenIntrospectionEndpointConfigurer.class));


            // 以下三个端点,不需要跳转到 其他地方,直接给出 未认证 ...
            // 这是默认行为 ...
            exceptionHandling
                    .defaultAuthenticationEntryPointFor(
                            new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                            new OrRequestMatcher(matchers));
        }

    }

    public void configure(B builder) {
        this.configurers.values().forEach((configurer) -> {
            configurer.configure(builder);
        });
        ProviderSettings providerSettings = AppAuthConfigurerUtils.getProviderSettings(builder).getProviderSettings();
        AuthorizationProviderContextFilter providerContextFilter = new AuthorizationProviderContextFilter(providerSettings);
        builder.addFilterAfter(this.postProcess(providerContextFilter), SecurityContextPersistenceFilter.class);
        JWKSource<SecurityContext> jwkSource = ProviderExtUtils.getJwkSource(builder);
        if (jwkSource != null) {
            AuthorizationServerNimbusJwkSetEndpointFilter jwkSetEndpointFilter = new AuthorizationServerNimbusJwkSetEndpointFilter(jwkSource, providerSettings.getJwkSetEndpoint());
            builder.addFilterBefore(this.postProcess(jwkSetEndpointFilter), AbstractPreAuthenticatedProcessingFilter.class);
        }

        AuthorizationServerMetadataEndpointFilter authorizationServerMetadataEndpointFilter = new AuthorizationServerMetadataEndpointFilter(providerSettings);
        builder.addFilterBefore(this.postProcess(authorizationServerMetadataEndpointFilter), AbstractPreAuthenticatedProcessingFilter.class);
    }

    private Map<Class<? extends AbstractAuthConfigurer>, AbstractAuthConfigurer> createConfigurers() {
        Map<Class<? extends AbstractAuthConfigurer>, AbstractAuthConfigurer> configurers = new LinkedHashMap<>();
        configurers.put(AuthTokenEndpointConfigurer.class, new AuthTokenEndpointConfigurer(this::postProcess));
        configurers.put(AuthTokenIntrospectionEndpointConfigurer.class, new AuthTokenIntrospectionEndpointConfigurer(this::postProcess));
        configurers.put(OpenConnectAuthServerMetadataConfigurer.class,new OpenConnectAuthServerMetadataConfigurer(this::postProcess));
        configurers.put(AuthTokenRevocationEndpointConfigurer.class, new AuthTokenRevocationEndpointConfigurer(this::postProcess));
        return configurers;
    }

    @SuppressWarnings("unchecked")
    private <T> T getConfigurer(Class<T> type) {
        return (T) this.configurers.get(type);
    }

    private <T extends AbstractAuthConfigurer> RequestMatcher getRequestMatcher(Class<T> configurerType) {
        return this.getConfigurer(configurerType).getRequestMatcher();
    }

    private void initEndpointMatchers(ProviderSettings providerSettings) {
        this.jwkSetEndpointMatcher = new AntPathRequestMatcher(providerSettings.getJwkSetEndpoint(), HttpMethod.GET.name());
        this.authorizationServerMetadataEndpointMatcher = new AntPathRequestMatcher(AUTHORIZATION_SERVER_METADATA_ENDPOINT_URL, HttpMethod.GET.name());
    }

    private static void validateProviderSettings(ProviderSettings providerSettings) {
        if (providerSettings.getIssuer() != null) {
            URI issuerUri;
            try {
                issuerUri = new URI(providerSettings.getIssuer());
                issuerUri.toURL();
            } catch (Exception var3) {
                throw new IllegalArgumentException("issuer must be a valid URL", var3);
            }

            if (issuerUri.getQuery() != null || issuerUri.getFragment() != null) {
                throw new IllegalArgumentException("issuer cannot contain query or fragment component");
            }
        }

    }
}
