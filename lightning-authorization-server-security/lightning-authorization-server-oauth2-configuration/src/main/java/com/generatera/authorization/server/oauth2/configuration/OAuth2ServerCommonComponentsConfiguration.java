package com.generatera.authorization.server.oauth2.configuration;

import com.generatera.authorization.server.common.configuration.AuthorizationServerCommonComponentsConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * oauth2 server configuration
 */
@Configuration
@AutoConfigureBefore(AuthorizationServerCommonComponentsConfiguration.class)
@EnableConfigurationProperties(AuthorizationServerOAuth2CommonComponentsProperties.class)
@Import(OAuth2ServerSwitchImportSelector.class)
public class OAuth2ServerCommonComponentsConfiguration {

//    @Bean
//    @ConditionalOnMissingBean(OAuth2TokenClaimsCustomizer.class)
//    public OAuth2TokenCustomizer<JwtEncodingContext> oauth2TokenClaimsCustomizer() {
//
//        LightningJwtCustomizerHandler defaultJwtCustomizerHandler = new DefaultJwtCustomizerHandler();
//        LightningJwtCustomizerHandler oauth2AuthenticationTokenJwtCustomizerHandler = new OAuth2AuthenticationTokenJwtCustomizerHandler(defaultJwtCustomizerHandler);
//        UsernamePasswordAuthenticationTokenJwtCustomizerHandler usernamePasswordAuthenticationTokenJwtCustomizerHandler = new UsernamePasswordAuthenticationTokenJwtCustomizerHandler(oauth2AuthenticationTokenJwtCustomizerHandler);
//        LightningJwtCustomizer jwtCustomizer = new JwtCustomizerImpl(usernamePasswordAuthenticationTokenJwtCustomizerHandler);
//
//        return jwtCustomizer::customizeToken;
//    }


    // 它需要解码器 ..



}
