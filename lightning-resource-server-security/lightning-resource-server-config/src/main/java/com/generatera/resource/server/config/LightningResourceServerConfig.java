package com.generatera.resource.server.config;

import com.generatera.authorization.server.common.configuration.LightningAuthServerConfigurer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

@Configuration
@AutoConfiguration
@EnableConfigurationProperties(ResourceServerProperties.class)
public class LightningResourceServerConfig {

    @ConditionalOnBean(type = "com.generatera.authorization.server.common.configuration.LightningAuthServerConfigurer")
    public static class HasAuthorizationServerConfiguration {
        @Bean
        public LightningAuthServerConfigurer resourceConfigurerBootstrap(
                List<LightningResourceServerConfigurer> configurers
        ) {
            return new LightningAuthServerConfigurer() {
                @Override
                public void configure(HttpSecurity securityBuilder) throws Exception {
                    for (LightningResourceServerConfigurer configurer : configurers) {
                        configurer.configure(securityBuilder);
                    }
                }
            };
        }
    }

   @ConditionalOnMissingBean(type = "com.generatera.authorization.server.common.configuration.LightningAuthServerConfigurer")
   public static class NoAuthorizationServerConfiguration {
       @Bean
       public SecurityFilterChain resourceServerBootstrap(HttpSecurity security,
                                                          List<LightningResourceServerConfigurer> configurers) throws Exception {
           for (LightningResourceServerConfigurer configurer : configurers) {
               configurer.configure(security);
           }

           security
                   .authorizeHttpRequests()
                   .anyRequest()
                   .authenticated();
           return security.build();
       }
   }

}
