package com.generatera.form.login.authorization.and.resource.server;

import com.generatera.authorization.application.server.config.DefaultLightningUserDetails;
import com.generatera.security.authorization.server.specification.JwtClaimsToUserPrincipalMapper;
import com.generatera.security.authorization.server.specification.LightningUserPrincipal;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.LightningJwt;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MyPlainJwtClaimsMapper implements JwtClaimsToUserPrincipalMapper {
    @Override
    public LightningUserPrincipal convert(LightningJwt jwt) {

        return new DefaultLightningUserDetails(User
                .withUsername(jwt.getSubject())
                .password("")
                .authorities(Optional.ofNullable(jwt.getClaimAsString("scope")).map(ele -> ele.split(",")).orElse(new String[0]))
                .build());

    }
}
