package com.generatera.oauth2.resource.server.config;


import com.generatera.authorization.server.common.configuration.authorization.LightningAuthorization;
import com.generatera.authorization.server.common.configuration.authorization.LightningAuthorizationService;
import com.generatera.oauth2.resource.server.config.authentication.OAuth2ResourceServerAuthenticationEntryPoint;
import com.generatera.oauth2.resource.server.config.token.LightningDefaultBearerTokenResolver;
import com.generatera.oauth2.resource.server.config.token.LightningDefaultHeaderBearerTokenResolver;
import com.generatera.resource.server.config.LightningResourceServerConfig;
import com.generatera.resource.server.config.LightningResourceServerConfigurer;
import com.generatera.resource.server.config.ResourceServerProperties;
import com.generatera.security.authorization.server.specification.BootstrapContext;
import com.generatera.security.authorization.server.specification.components.authentication.LightningLogoutHandler;
import com.generatera.security.authorization.server.specification.components.token.LightningTokenType;
import com.generatera.security.authorization.server.specification.util.HttpSecurityBuilderUtils;
import com.generatera.security.authorization.server.specification.util.LogUtil;
import com.jianyue.lightning.boot.starter.util.ElvisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

/**
 * token 解析注册 ...
 * 1. jwt
 * 1.1 bearer token
 * 1.2 opaque token
 * 需要的依赖 spring-security-oauth2-jose
 * <p>
 * 当oauth2 authorization server(这里指的是非 central) 本身就是 resource server时,我们需要做额外的处理 ..
 * 这种情况下, oauth2 authorization client server 会提供ProviderSettings等共有的信息 ..
 */
@Slf4j
@EnableConfigurationProperties(OAuth2ResourceServerProperties.class)
@AutoConfigureAfter(LightningResourceServerConfig.class)
@Import(OAuth2ResourceServerComponentsImportSelector.class)
public class OAuth2ResourceServerConfiguration {

    /**
     * 仅仅只是打印 log...
     */
    @Bean
    public LightningResourceServerConfigurer oauth2ResourceServerConfigurer(
            ResourceServerProperties properties,
            BearerTokenResolver tokenResolver
    ) {
        return new LightningResourceServerConfigurer() {
            @Override
            public void configure(HttpSecurity security) throws Exception {
                OAuth2ResourceServerConfigurer<HttpSecurity> configurer = security.oauth2ResourceServer();
                OAuth2ResourceServerAuthenticationEntryPoint oAuth2ResourceServerAuthenticationEntryPoint = new OAuth2ResourceServerAuthenticationEntryPoint();
                ElvisUtil.isNotEmptyConsumer(properties.getAuthorityConfig().getInvalidTokenErrorMessage(), oAuth2ResourceServerAuthenticationEntryPoint::setInvalidTokenErrorMessage);
                ElvisUtil.isNotEmptyConsumer(properties.getAuthorityConfig().getFilterAccessDeniedErrorMessage(), oAuth2ResourceServerAuthenticationEntryPoint::setAccessDeniedErrorMessage);

                // method security access denied > SpringSecurityMessageSource.getAccessor() 已经处理的非常好了 ...

                // authentication entry point 配置
                configurer.authenticationEntryPoint(oAuth2ResourceServerAuthenticationEntryPoint);

               configurer.bearerTokenResolver(tokenResolver);


                LogUtil.prettyLog("oauth2 resource server enabled !!!!");
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public BearerTokenResolver bearerTokenResolver(ResourceServerProperties properties) {
        String authorizationHeader = ElvisUtil.stringElvis(properties.getTokenVerificationConfig().getBearerTokenConfig().getTokenIdentifier(), "Authorization");
        if (properties.getTokenVerificationConfig().getBearerTokenConfig().isUseHeader()) {
            // header 直接解析
           return  new LightningDefaultHeaderBearerTokenResolver(authorizationHeader);
        }
        else {

            return new LightningDefaultBearerTokenResolver(authorizationHeader);
        }
    }

    /**
     * 这里假设,中央授权服务器和 应用授权服务器将不在 同一个服务器中,否则会存在多个 LightningAuthorizationService ..
     * // 然而我们不知道如何处理哪一个 LightningAuthorizationService的退出 ...
     * @param bootstrapContext 引导 上下文
     *
     * 仅当 存在授权服务器的情况下,才需要加入此配置 ...
     */
    @Bean
    @ConditionalOnClass(LightningAuthorizationService.class)
    public LightningLogoutHandler defaultLogoutHandler(BootstrapContext bootstrapContext,BearerTokenResolver tokenResolver) {
        // 如果有多个自然会报错
        try {
            LightningAuthorizationService bean = HttpSecurityBuilderUtils.getBean(Objects.requireNonNull(bootstrapContext.get(HttpSecurity.class)), LightningAuthorizationService.class);

            return new LightningLogoutHandler() {
                @Override
                @SuppressWarnings("unchecked")
                public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
                    String  currentAccessToken = tokenResolver.resolve(request);
                    if(StringUtils.hasText(currentAccessToken)) {
                        // 不需要 authorization 实际类型
                        LightningAuthorization byToken = bean.findByToken(currentAccessToken, LightningTokenType.LightningAuthenticationTokenType.ACCESS_TOKEN_TYPE);
                        if(byToken != null) {
                            bean.remove(byToken);
                        }
                    }
                }
            };
        }catch (Exception e) {
            // 中央授权服务器 不能和应用授权服务器在同一个服务器中 ..
            throw new IllegalArgumentException("Multiple authorization service were found, but only one is needed!!!");
        }
    }

}
