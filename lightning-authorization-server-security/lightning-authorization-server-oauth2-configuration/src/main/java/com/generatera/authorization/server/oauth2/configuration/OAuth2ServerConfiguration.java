//package com.generatera.authorization.server.oauth2.configuration;
//
//import com.generatera.authorization.server.common.configuration.token.customizer.jwt.LightningJwtCustomizer;
//import com.generatera.authorization.server.common.configuration.token.customizer.jwt.LightningJwtCustomizerHandler;
//import com.generatera.authorization.server.common.configuration.token.customizer.jwt.impl.DefaultJwtCustomizerHandler;
//import com.generatera.authorization.server.common.configuration.token.customizer.jwt.impl.JwtCustomizerImpl;
//import com.generatera.authorization.server.common.configuration.token.customizer.jwt.impl.OAuth2AuthenticationTokenJwtCustomizerHandler;
//import com.generatera.authorization.server.common.configuration.token.customizer.jwt.impl.UsernamePasswordAuthenticationTokenJwtCustomizerHandler;
//import com.generatera.authorization.server.common.configuration.token.customizer.token.claims.OAuth2TokenClaimsCustomizer;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
//import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
//
///**
// * oauth2 server configuration
// */
//@Configuration
//public class OAuth2ServerConfiguration {
//
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
//
//}
