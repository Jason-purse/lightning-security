//package com.generatera.central.oauth2.authorization.server.configuration;
//
//import org.springframework.boot.autoconfigure.AutoConfiguration;
//import org.springframework.context.annotation.Bean;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
//
//@AutoConfiguration
//public class TokenIntrospectConfiguration {
//
//    @Bean
//    public LightningOAuth2CentralAuthorizationServerExtConfigurer auth2CentralAuthorizationServerExtConfigurer() {
//        return new LightningOAuth2CentralAuthorizationServerExtConfigurer() {
//            @Override
//            public void configure(OAuth2AuthorizationServerConfigurer<HttpSecurity> configurer) throws Exception {
//                configurer.tokenIntrospectionEndpoint(
//                        endpoint -> {
//                            endpoint.introspectionResponseHandler()
//                        }
//                )
//            }
//        }
//    }
//}
