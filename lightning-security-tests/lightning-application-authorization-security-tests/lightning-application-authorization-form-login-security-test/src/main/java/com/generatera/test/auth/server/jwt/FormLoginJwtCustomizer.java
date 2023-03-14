package com.generatera.test.auth.server.jwt;

import com.generatera.security.authorization.server.specification.components.token.format.jwt.JwtClaimsSet;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.JwtEncodingContext;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.customizer.LightningJwtCustomizer;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

/**
 * 尝试定制  jwt 内容
 */
@Component
public class FormLoginJwtCustomizer  implements LightningJwtCustomizer {
    @Override
    public void customizeToken(JwtEncodingContext context) {
        JwtClaimsSet.Builder claims = context.getClaims();
        List<String> scope = claims.getClaimAsStringList("scope");
        if(scope == null) {
            scope = new LinkedList<>();
        }
        else {
            scope = new LinkedList<>(scope);
        }

        // 增加一个权限点

        scope.add("ROLE_role1");
        scope.add("ROLE_role2");
        scope.add("ROLE_role3");

        scope.add("123");
        scope.add("456");
        scope.add("789");
        scope.add("124");
        scope.add("127");
        scope.add("");


        claims.claim("authorities",scope);
        claims.removeClaim("scope");
    }
}
