package com.generatera.resource.server.config;

import com.generatera.authorization.server.common.configuration.LightningAuthServerConfigurer;
import com.generatera.resource.server.config.util.TokenAwareRestTemplate;
import com.jianyue.lightning.boot.starter.util.ElvisUtil;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
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
                List<LightningResourceServerConfigurer> configurers,
                ResourceServerProperties properties
                ) {

            return new LightningAuthServerConfigurer() {
                @Override
                public void configure(HttpSecurity securityBuilder) throws Exception {
                    for (LightningResourceServerConfigurer configurer : configurers) {
                        configurer.configure(securityBuilder);
                    }

                    urlWhilteListHandle(securityBuilder, properties);
                    // 不需要fallback 兜底,服务器自动存在兜底
                }
            };
        }
    }

    @ConditionalOnMissingBean(type = "com.generatera.authorization.server.common.configuration.LightningAuthServerConfigurer")
    public static class NoAuthorizationServerConfiguration {
        @Bean
        public SecurityFilterChain resourceServerBootstrap(HttpSecurity security,
                                                           ResourceServerProperties resourceServerProperties,
                                                           List<LightningResourceServerConfigurer> configurers) throws Exception {
            for (LightningResourceServerConfigurer configurer : configurers) {
                configurer.configure(security);
            }

            // 白名单处理
            urlWhilteListHandle(security, resourceServerProperties);
            defaultFallbackUrlWhiteListHandle(security);
            return security.build();
        }
    }

    private static void urlWhilteListHandle(HttpSecurity security, ResourceServerProperties resourceServerProperties) throws Exception {
        security.apply(new SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity>() {
            @Override
            public void configure(HttpSecurity builder) throws Exception {

                List<String> urlWhiteList = resourceServerProperties.getPermission().getUrlWhiteList();
                ElvisUtil.isNotEmptyConsumer(urlWhiteList, list -> {
                    try {
                        builder.authorizeHttpRequests()
                                .antMatchers(list.toArray(String[]::new))
                                .permitAll();
                    } catch (Exception e) {
                        // 无法放行 ...
                        throw new RuntimeException(e);
                    }
                });
            }
        });
    }

    private static void defaultFallbackUrlWhiteListHandle(HttpSecurity security) throws Exception {
        security.apply(
                new SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity>() {
                    @Override
                    public void configure(HttpSecurity builder) throws Exception {
                        builder.authorizeHttpRequests()
                                .anyRequest()
                                .authenticated();
                    }
                }
        );
    }

    @Bean
    @ConditionalOnMissingBean(TokenAwareRestTemplate.class)
    public TokenAwareRestTemplate tokenAwareRestTemplate() {
        return new TokenAwareRestTemplate();
    }

}
