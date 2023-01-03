package com.generatera.authorization.server.common.configuration;

import com.generatera.authorization.server.common.configuration.token.customizer.jwt.LightningJwtCustomizer;
import com.generatera.authorization.server.common.configuration.token.customizer.jwt.LightningJwtCustomizerHandler;
import com.generatera.authorization.server.common.configuration.token.customizer.jwt.impl.JwtCustomizerImpl;
import com.generatera.authorization.server.common.configuration.token.customizer.token.claims.OAuth2TokenClaimsCustomizer;
import com.generatera.authorization.server.common.configuration.token.customizer.token.claims.impl.OAuth2TokenClaimsCustomizerImpl;
import com.generatera.authorization.server.common.configuration.util.jose.Jwks;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenClaimsContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

/**
 * 授权服务器的 通用组件配置
 */
@Configuration
@AutoConfigureBefore(OAuth2AuthorizationServerConfiguration.class)
@EnableConfigurationProperties(AuthorizationServerComponentProperties.class)
@Import(AuthorizationServerComponentImportSelector.class)
public class AuthorizationServerCommonComponentsConfiguration {

    /**
     * jwke set
     */
    @Bean
    @ConditionalOnMissingBean(JWKSource.class)
    public JWKSource<SecurityContext> jwkSource() {
        RSAKey rsaKey = Jwks.generateRsa();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
    }


    @Bean
    @ConditionalOnMissingBean(JwtDecoder.class)
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }


    // Token 自定义 ..


    @Bean
    @ConditionalOnMissingBean(OAuth2TokenClaimsCustomizer.class)
    public OAuth2TokenCustomizer<JwtEncodingContext> buildJwtCustomizer() {


    }

    /**
     * oauth2 token 自定义器
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(OAuth2TokenClaimsCustomizer.class)
    public OAuth2TokenCustomizer<OAuth2TokenClaimsContext> buildOAuth2TokenClaimsCustomizer() {

        OAuth2TokenClaimsCustomizer oauth2TokenClaimsCustomizer = new OAuth2TokenClaimsCustomizerImpl();

        return oauth2TokenClaimsCustomizer::customizeTokenClaims;
    }

}
