package com.generatera.resource.server.specification.token.jwt.config;

import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.util.Assert;

public class JwtConfigurer {
        private final ApplicationContext context;
        private AuthenticationManager authenticationManager;
        private LightningJwtDecoder decoder;
        private Converter<LightningJwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter;

        JwtConfigurer(ApplicationContext context) {
            this.context = context;
        }

        public JwtConfigurer authenticationManager(AuthenticationManager authenticationManager) {
            Assert.notNull(authenticationManager, "authenticationManager cannot be null");
            this.authenticationManager = authenticationManager;
            return this;
        }

        public JwtConfigurer decoder(LightningJwtDecoder decoder) {
            this.decoder = decoder;
            return this;
        }

        public JwtConfigurer jwkSetUri(String uri) {
            this.decoder = NimbusJwtDecoder.withJwkSetUri(uri).build();
            return this;
        }

        public JwtConfigurer jwtAuthenticationConverter(Converter<LightningJwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter) {
            this.jwtAuthenticationConverter = jwtAuthenticationConverter;
            return this;
        }

        Converter<LightningJwt, ? extends AbstractAuthenticationToken> getJwtAuthenticationConverter() {
            if (this.jwtAuthenticationConverter == null) {
                if (this.context.getBeanNamesForType(JwtAuthenticationConverter.class).length > 0) {
                    this.jwtAuthenticationConverter = this.context.getBean(JwtAuthenticationConverter.class);
                } else {
                    this.jwtAuthenticationConverter = new JwtAuthenticationConverter();
                }
            }

            return this.jwtAuthenticationConverter;
        }

        LightningJwtDecoder getJwtDecoder() {
            return this.decoder == null ? (LightningJwtDecoder)this.context.getBean(LightningJwtDecoder.class) : this.decoder;
        }

        //AuthenticationProvider getAuthenticationProvider() {
        //    if (this.authenticationManager != null) {
        //        return null;
        //    } else {
        //        LightningJwtDecoder decoder = this.getJwtDecoder();
        //        Converter<LightningJwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter = this.getJwtAuthenticationConverter();
        //        JwtAuthenticationProvider provider = new JwtAuthenticationProvider(decoder);
        //        provider.setJwtAuthenticationConverter(jwtAuthenticationConverter);
        //        return (AuthenticationProvider)LightningResourceServerConfigurer.this.postProcess(provider);
        //    }
        //}
        //
        //AuthenticationManager getAuthenticationManager(H http) {
        //    return this.authenticationManager != null ? this.authenticationManager : (AuthenticationManager)http.getSharedObject(AuthenticationManager.class);
        //}
    }