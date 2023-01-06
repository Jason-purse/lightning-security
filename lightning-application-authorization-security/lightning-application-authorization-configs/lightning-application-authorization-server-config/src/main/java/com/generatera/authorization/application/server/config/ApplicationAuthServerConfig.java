package com.generatera.authorization.application.server.config;

import com.generatera.security.authorization.server.specification.authentication.LightningSecurityContextRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

@Configuration
@AutoConfigureBefore(SecurityAutoConfiguration.class)
@EnableConfigurationProperties(ApplicationAuthServerProperties.class)
@Import({ApplicationAuthServerSpecificationComponentImportSelector.class})
public class ApplicationAuthServerConfig {

    @Bean
    @ConditionalOnMissingBean(AuthExtSecurityConfigurer.class)
    public AuthExtSecurityConfigurer oAuth2ExtSecurityConfigurer(ApplicationAuthServerProperties appAuthProperties,
                                                                 @Autowired(required = false) List<LightningAppAuthServerConfigurer> configurers) {
        return new AuthExtSecurityConfigurer(appAuthProperties,configurers);
    }

    //@Bean
    //@ConditionalOnMissingBean(LightningSecurityContextRepository.class)
    //public LightningSecurityContextRepository securityContextRepository() {
    //    return new DefaultSecurityContextRepository();
    //}

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain httpSecurity(HttpSecurity httpSecurity,
                                            @Autowired(required = false)
                                            LightningSecurityContextRepository repository,
                                            AuthExtSecurityConfigurer configurer) throws Exception {
        return httpSecurity
                .apply(configurer)
                .and()
                .sessionManagement()
                .disable()
                .securityContext()
                //.securityContextRepository(repository)
                .and()
                .apply(new SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity>() {
                    @Override
                    public void init(HttpSecurity builder) throws Exception {
                        // 最后添加这个
                        builder
                                .authorizeHttpRequests()
                                .anyRequest()
                                .authenticated()
                                .and()
                                .csrf()
                                .disable();
                    }
                })
                .and()
                .build();
    }

}
