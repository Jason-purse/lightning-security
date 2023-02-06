package com.generatera.test.auth.server.jwt;

import com.generatera.security.authorization.server.specification.components.token.format.jwt.JwtClaimsSet;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.JwtEncodingContext;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.customizer.LightningJwtCustomizer;
import org.springframework.stereotype.Component;

/**
 * 尝试定制  jwt 内容
 */
@Component
public class FormLoginJwtCustomizer  implements LightningJwtCustomizer {
    @Override
    public void customizeToken(JwtEncodingContext context) {
        JwtClaimsSet.Builder claims = context.getClaims();
        claims.claim("authorities",claims.getClaim("scope"));
        claims.removeClaim("scope");
    }
}
