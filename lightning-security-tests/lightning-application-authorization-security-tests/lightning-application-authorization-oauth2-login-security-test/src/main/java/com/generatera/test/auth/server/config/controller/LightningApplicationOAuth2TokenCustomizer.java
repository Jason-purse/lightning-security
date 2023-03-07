package com.generatera.test.auth.server.config.controller;

import com.generatera.LightningUserDetails;
import com.generatera.security.authorization.server.specification.LightningUserPrincipal;
import com.generatera.security.authorization.server.specification.components.token.*;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.JwtClaimsSet;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.JwtEncodingContext;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.customizer.LightningJwtCustomizer;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

/**
 * @author FLJ
 * @date 2023/1/13
 * @time 12:31
 * @Description oauth2 Token 定制化器
 */
@Component
public class LightningApplicationOAuth2TokenCustomizer implements LightningJwtCustomizer {

//    @Override
//    public void customize(LightningTokenContext context) {
//        LightningUserDetails principal = (LightningUserDetails)context.getPrincipal();
//        if (context instanceof JwtEncodingContext jwtEncodingContext) {
//            if ( LightningTokenType.LightningAuthenticationTokenType.ACCESS_TOKEN_TYPE.value().equalsIgnoreCase(context.getTokenType().value())) {
//                JwtClaimsSet.Builder claims = jwtEncodingContext.getClaims();
//                claims.claim("openid", principal.getUsername());
//            }
//
//        } else if (context instanceof LightningTokenClaimsContext tokenClaimsContext) {
//            if (LightningTokenType.LightningAuthenticationTokenType.ACCESS_TOKEN_TYPE.value().equalsIgnoreCase(context.getTokenType().value())) {
//                LightningTokenClaimsSet.Builder claims = tokenClaimsContext.getClaims();
//                claims.claim("openid", principal.getUsername());
//            }
//        }
//    }

    @Override
    public void customizeToken(JwtEncodingContext context) {
        LightningUserDetails principal = (LightningUserDetails) context.getPrincipal();
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


        claims.claim("authorities",scope);
        claims.removeClaim("scope");
    }
}
