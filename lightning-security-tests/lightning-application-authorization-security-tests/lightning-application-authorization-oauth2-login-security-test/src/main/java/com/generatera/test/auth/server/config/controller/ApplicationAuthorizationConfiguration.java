package com.generatera.test.auth.server.config.controller;

import org.springframework.context.annotation.Bean;

/**
 * @author Sun.
 */
public class ApplicationAuthorizationConfiguration {

    @Bean
    public LightningApplicationOAuth2TokenCustomizer applicationOAuth2TokenCustomizer() {
        return new LightningApplicationOAuth2TokenCustomizer();
    }

}
