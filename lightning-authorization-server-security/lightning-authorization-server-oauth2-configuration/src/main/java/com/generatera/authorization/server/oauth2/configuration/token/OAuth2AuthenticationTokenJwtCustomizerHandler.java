//package com.generatera.authorization.server.oauth2.configuration.token;
//
//import com.generatera.authorization.server.common.configuration.token.customizer.jwt.JwtClaimsSet;
//import com.generatera.authorization.server.common.configuration.token.customizer.jwt.JwtEncodingContext;
//import com.generatera.authorization.server.common.configuration.token.customizer.jwt.LightningJwtCustomizerHandler;
//import com.generatera.authorization.server.oauth2.configuration.token.impl.AbstractJwtCustomizerHandler;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
//import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
//import org.springframework.security.oauth2.core.oidc.OidcIdToken;
//import org.springframework.security.oauth2.core.oidc.user.OidcUser;
//import org.springframework.security.oauth2.core.user.OAuth2User;
//
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Set;
//
//public class OAuth2AuthenticationTokenJwtCustomizerHandler extends AbstractJwtCustomizerHandler implements OAuth2JwtCustomizerHandler {
//
//    private final Set<String> ID_TOKEN_CLAIMS = Set.of(
//            IdTokenClaimNames.ISS,
//            IdTokenClaimNames.SUB,
//            IdTokenClaimNames.AUD,
//            IdTokenClaimNames.EXP,
//            IdTokenClaimNames.IAT,
//            IdTokenClaimNames.AUTH_TIME,
//            IdTokenClaimNames.NONCE,
//            IdTokenClaimNames.ACR,
//            IdTokenClaimNames.AMR,
//            IdTokenClaimNames.AZP,
//            IdTokenClaimNames.AT_HASH,
//            IdTokenClaimNames.C_HASH);
//
//    public OAuth2AuthenticationTokenJwtCustomizerHandler(LightningJwtCustomizerHandler jwtCustomizerHandler) {
//        super(jwtCustomizerHandler);
//    }
//
//    @Override
//    protected void customizeJwt(JwtEncodingContext jwtEncodingContext) {
//
//        Authentication authentication = jwtEncodingContext.getPrincipal();
//
//        Map<String, Object> thirdPartyClaims = extractClaims(authentication);
//
//        JwtClaimsSet.Builder jwtClaimSetBuilder = jwtEncodingContext.getClaims();
//
//        jwtClaimSetBuilder.claims(existingClaims -> {
//
//            // Remove conflicting claims set by this authorization server
//            existingClaims.keySet().forEach(thirdPartyClaims::remove);
//
//            // Remove standard id_token claims that could cause problems with clients
//            ID_TOKEN_CLAIMS.forEach(thirdPartyClaims::remove);
//
//            // Add all other claims directly to id_token
//            existingClaims.putAll(thirdPartyClaims);
//        });
//
//    }
//
//    private Map<String, Object> extractClaims(Authentication authentication) {
//        Map<String, Object> claims;
//        Object principalObj = authentication.getPrincipal();
//        if (principalObj instanceof OidcUser oidcUser) {
//            OidcIdToken idToken = oidcUser.getIdToken();
//            claims = idToken.getClaims();
//        } else if (principalObj instanceof OAuth2User oauth2User) {
//            claims = oauth2User.getAttributes();
//        } else {
//            claims = Collections.emptyMap();
//        }
//
//        return new HashMap<>(claims);
//    }
//
//    @Override
//    protected boolean supportCustomizeContext(Authentication authentication) {
//        return authentication instanceof OAuth2AuthenticationToken;
//    }
//
//}
