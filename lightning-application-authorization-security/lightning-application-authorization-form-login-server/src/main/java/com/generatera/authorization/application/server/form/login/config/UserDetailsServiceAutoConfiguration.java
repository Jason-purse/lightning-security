package com.generatera.authorization.application.server.form.login.config;

import com.generatera.authorization.application.server.config.DefaultLightningUserDetails;
import com.generatera.security.authorization.server.specification.LightningUserPrincipal;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 遵循  UserDetailsService 返回 LightningUserPrincipal的原则
 */
@Configuration
public class UserDetailsServiceAutoConfiguration {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public GlobalAuthenticationConfigurerAdapter daoAuthenticationProvider(ApplicationContext context) {
        return new GlobalAuthenticationConfigurerAdapter() {
            @Override
            public void configure(AuthenticationManagerBuilder auth) throws Exception {
                if (!auth.isConfigured()) {
                    UserDetailsService userDetailsService = this.getBeanOrNull(UserDetailsService.class);
                    if (userDetailsService != null) {

                        UserDetailsService finalUserDetailsService = userDetailsService;
                        userDetailsService = username -> {
                            UserDetails userDetails = finalUserDetailsService.loadUserByUsername(username);
                            if(!LightningUserPrincipal.class.isAssignableFrom(userDetails.getClass())) {
                                return new DefaultLightningUserDetails(userDetails);
                            }
                            return userDetails;
                        };

                        PasswordEncoder passwordEncoder = this.getBeanOrNull(PasswordEncoder.class);
                        UserDetailsPasswordService passwordManager = this.getBeanOrNull(UserDetailsPasswordService.class);
                        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
                        provider.setUserDetailsService(userDetailsService);
                        if (passwordEncoder != null) {
                            provider.setPasswordEncoder(passwordEncoder);
                        }

                        if (passwordManager != null) {
                            provider.setUserDetailsPasswordService(passwordManager);
                        }

                        provider.afterPropertiesSet();
                        auth.authenticationProvider(provider);
                    }
                }
            }

            private <T> T getBeanOrNull(Class<T> type) {
                String[] beanNames = context.getBeanNamesForType(type);
                return beanNames.length != 1 ? null : context.getBean(beanNames[0], type);
            }
        };
    }


    @ConditionalOnMissingBean(UserDetailsService.class)
    public static class  NoUserDetailsServiceConfiguration {

        private static final String NOOP_PASSWORD_PREFIX = "{noop}";
        private static final Pattern PASSWORD_ALGORITHM_PATTERN = Pattern.compile("^\\{.+}.*$");
        private static final Log logger = LogFactory.getLog(UserDetailsServiceAutoConfiguration.class);

        public NoUserDetailsServiceConfiguration() {
        }

        @Bean
        @Lazy
        public InMemoryUserDetailsManager inMemoryUserDetailsManager(SecurityProperties properties, ObjectProvider<PasswordEncoder> passwordEncoder) {
            SecurityProperties.User user = properties.getUser();
            List<String> roles = user.getRoles();
            return new DefaultInMemoryUserDetailsManager(
                    org.springframework.security.core.userdetails.User
                            .withUsername(user.getName())
                            .password(this.getOrDeducePassword(user, passwordEncoder.getIfAvailable()))
                            .roles(StringUtils.toStringArray(roles))
                            .build()
            );
        }

        private String getOrDeducePassword(SecurityProperties.User user, PasswordEncoder encoder) {
            String password = user.getPassword();
            if (user.isPasswordGenerated()) {
                logger.warn(String.format("%n%nUsing generated security password: %s%n%nThis generated password is for development use only. Your security configuration must be updated before running your application in production.%n", user.getPassword()));
            }

            return encoder == null && !PASSWORD_ALGORITHM_PATTERN.matcher(password).matches() ? "{noop}" + password : password;
        }
    }

}

class DefaultInMemoryUserDetailsManager extends InMemoryUserDetailsManager {
    public DefaultInMemoryUserDetailsManager(UserDetails... userDetails) {
        super(userDetails);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return new DefaultLightningUserDetails(super.loadUserByUsername(username));
    }
}