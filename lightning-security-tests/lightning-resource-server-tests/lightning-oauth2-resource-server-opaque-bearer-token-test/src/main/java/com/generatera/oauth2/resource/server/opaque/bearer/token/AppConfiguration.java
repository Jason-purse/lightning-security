package com.generatera.oauth2.resource.server.opaque.bearer.token;

import com.generatera.security.authorization.server.specification.components.token.JwtClaimsToUserPrincipalMapper;
import com.generatera.security.authorization.server.specification.LightningUserPrincipal;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.jwt.JwtClaimNames;

import java.util.Collection;
import java.util.Map;

@Configuration
public class AppConfiguration {

    @Bean
    JwtClaimsToUserPrincipalMapper userPrincipalMapper() {
        return new JwtClaimsToUserPrincipalMapper() {
            @Override
            public LightningUserPrincipal convert(Map<String, Object> claims) {
                User.UserBuilder builder = SimpleUserPrincipal.withUsername(
                                claims.get(JwtClaimNames.SUB).toString()
                        )
                        .password("");
                Object authorties = claims.get("authorities");
                if (authorties != null) {
                    builder.authorities(((Collection<? extends GrantedAuthority>) authorties));
                }

                return new SimpleUserPrincipal(builder.build());
            }
        };
    }
}
