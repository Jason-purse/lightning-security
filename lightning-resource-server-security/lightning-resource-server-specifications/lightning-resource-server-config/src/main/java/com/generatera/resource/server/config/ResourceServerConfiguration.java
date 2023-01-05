package com.generatera.resource.server.config;

import com.generatera.resource.server.config.bootstrap.DefaultResourceServerConfigurer;
import com.generatera.resource.server.config.token.LightningTokenAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import java.util.List;

@Configuration
public class ResourceServerConfiguration {

    /**
     * resource server的配置器
     */
    @Bean
    @ConditionalOnMissingBean(LightningResourceServerConfigurer.class)
    public LightningResourceServerConfigurer resourceServerConfigurer(
            LightningTokenAuthenticationFilter authenticationFilter,
            @Autowired(required = false)
            List<LightningResourceServerOtherConfigurer> configurers) {
        return new LightningResourceServerConfigurer() {
            @Override
            public void configure(DefaultResourceServerConfigurer<HttpSecurity> resourceServerConfigurer) {
                resourceServerConfigurer.tokenAuthenticationFilter(authenticationFilter);
                if(configurers != null && configurers.size() > 0) {
                    for (LightningResourceServerOtherConfigurer configurer : configurers) {
                        configurer.configure(resourceServerConfigurer.and());
                    }
                }
            }
        };
    }

}
