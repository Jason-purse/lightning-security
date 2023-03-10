package com.generatera.resource.server.config;

import com.generatera.authorization.server.common.configuration.LightningAuthServerConfigurer;
import com.generatera.security.authorization.server.specification.BootstrapContext;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

/**
 * 资源服务器 配置
 */
@AutoConfiguration
@AutoConfigureBefore(OAuth2ResourceServerAutoConfiguration.class)
@EnableConfigurationProperties(ResourceServerProperties.class)
@Import({LightningGlobalMethodSecurityConfiguration.class})
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

            // 加入共享对象 ...
            BootstrapContext.fromHttpSecurity(security);

            security
                    .authorizeHttpRequests()
                    .anyRequest()
                    .authenticated();
            return security.build();
        }
    }

}
