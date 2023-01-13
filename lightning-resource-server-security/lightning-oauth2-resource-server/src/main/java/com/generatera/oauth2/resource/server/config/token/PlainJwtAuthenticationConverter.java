package com.generatera.oauth2.resource.server.config.token;

import com.generatera.security.authorization.server.specification.JwtClaimsToUserPrincipalMapper;
import com.generatera.security.authorization.server.specification.LightningUserPrincipal;
import com.generatera.security.authorization.server.specification.components.token.LightningTokenType;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.LightningJwt;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

/**
 * @author FLJ
 * @date 2023/1/12
 * @time 13:09
 * @Description 将JWT 转换为 LightningUserPrincipal ...
 */
public class PlainJwtAuthenticationConverter extends LightningJwtAuthenticationConverter {

    private JwtClaimsToUserPrincipalMapper jwtClaimsMapper;

    public PlainJwtAuthenticationConverter() {

    }

    public void setJwtClaimsMapper(JwtClaimsToUserPrincipalMapper jwtClaimsMapper) {
        this.jwtClaimsMapper = jwtClaimsMapper;
    }

    @Override
    public AbstractAuthenticationToken convert(@NotNull Jwt jwt) {
        AbstractAuthenticationToken authenticationToken = super.convert(jwt);
        assert authenticationToken != null;
        if (jwtClaimsMapper != null) {

            // 代码转换 ...
            LightningJwt lightningJwt = new LightningJwt(
                    new LightningTokenType.LightningTokenValueType(
                            Optional
                                    .ofNullable(jwt.getClaimAsString("tokenValueType"))
                                    // 目前系统可能仅仅支持 Bearer ...
                                    .orElse(LightningTokenType.LightningTokenValueType.BEARER_TOKEN_TYPE.value())
                    ),
                    jwt.getTokenValue(),
                    jwt.getIssuedAt(),
                    jwt.getExpiresAt(),
                    jwt.getHeaders(),
                    jwt.getClaims()
            );
            LightningUserPrincipal userPrincipal = jwtClaimsMapper.convert(lightningJwt.getClaims());
            return new UsernamePasswordAuthenticationToken(
                    userPrincipal,
                    authenticationToken.getCredentials(),
                    userPrincipal.getAuthorities()
            );
        }
        return authenticationToken;

    }
}
