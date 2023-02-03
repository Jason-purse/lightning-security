package com.generatera.oauth2.resource.server.config;


import com.generatera.oauth2.resource.server.config.token.LightningAuthenticationTokenResolver;
import com.generatera.resource.server.config.LightningResourceServerConfig;
import com.generatera.resource.server.config.LightningResourceServerConfigurer;
import com.generatera.resource.server.config.LogUtil;
import com.generatera.resource.server.config.ResourceServerProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.oauth2.server.resource.web.HeaderBearerTokenResolver;

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
            ResourceServerProperties properties
    ) {
        return new LightningResourceServerConfigurer() {
            @Override
            public void configure(HttpSecurity security) throws Exception {
                OAuth2ResourceServerConfigurer<HttpSecurity> configurer = security.oauth2ResourceServer();
                if (properties.getTokenVerificationConfig().getBearerTokenConfig().isUseHeader()) {
                    // header 直接解析
                    configurer.bearerTokenResolver(
                            new HeaderBearerTokenResolver(LightningAuthenticationTokenResolver.TOKEN_IDENTITY_NAME));
                }

                LogUtil.prettyLog("oauth2 resource server enabled !!!!");
            }
        };
    }

}
