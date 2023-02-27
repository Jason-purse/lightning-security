package com.generatera.authorization.application.server.form.login.config.components;

import com.generatera.security.authorization.server.specification.DefaultLightningUserDetails;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
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

    @ConditionalOnMissingBean(UserDetailsService.class)
    public static class  NoUserDetailsServiceConfiguration {

        private static final String NOOP_PASSWORD_PREFIX = "{noop}";
        private static final Pattern PASSWORD_ALGORITHM_PATTERN = Pattern.compile("^\\{.+}.*$");
        private static final Log logger = LogFactory.getLog(UserDetailsServiceAutoConfiguration.class);

        public NoUserDetailsServiceConfiguration() {
        }

        @Bean
        @Lazy
        public LightningUserDetailService inMemoryUserDetailsManager(SecurityProperties properties, ObjectProvider<PasswordEncoder> passwordEncoder) {
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

class DefaultInMemoryUserDetailsManager extends InMemoryUserDetailsManager implements LightningUserDetailService {
    public DefaultInMemoryUserDetailsManager(UserDetails... userDetails) {
        super(userDetails);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return new DefaultLightningUserDetails(super.loadUserByUsername(username));
    }

}