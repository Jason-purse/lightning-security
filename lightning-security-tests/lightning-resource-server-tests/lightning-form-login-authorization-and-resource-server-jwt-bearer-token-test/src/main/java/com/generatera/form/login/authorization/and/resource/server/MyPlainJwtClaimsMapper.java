package com.generatera.form.login.authorization.and.resource.server;

import com.generatera.security.authorization.server.specification.DefaultLightningUserDetails;
import com.generatera.security.authorization.server.specification.components.token.JwtClaimsToUserPrincipalMapper;
import com.generatera.security.authorization.server.specification.LightningUserPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class MyPlainJwtClaimsMapper implements JwtClaimsToUserPrincipalMapper {
    @Override
    public LightningUserPrincipal convert(Map<String, Object> claims) {

        return new DefaultLightningUserDetails(User
                .withUsername(claims.get(JwtClaimNames.SUB).toString())
                .password("")
                .authorities(
                        Optional
                                .ofNullable(
                                        claims.get("scope").toString()).map(ele -> ele.split(","))
                                .orElse(new String[0]))
                .build());

    }
}
