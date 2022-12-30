package com.generatera.authorization.ext.oauth2.customizer.jwt.impl;

import com.generatera.authorization.ext.oauth2.customizer.jwt.JwtCustomizerHandler;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;

import java.util.*;

public class OAuth2AuthenticationTokenJwtCustomizerHandler extends AbstractJwtCustomizerHandler {

    private final Set<String> ID_TOKEN_CLAIMS = Set.of(
            IdTokenClaimNames.ISS,
            IdTokenClaimNames.SUB,
            IdTokenClaimNames.AUD,
            IdTokenClaimNames.EXP,
            IdTokenClaimNames.IAT,
            IdTokenClaimNames.AUTH_TIME,
            IdTokenClaimNames.NONCE,
            IdTokenClaimNames.ACR,
            IdTokenClaimNames.AMR,
            IdTokenClaimNames.AZP,
            IdTokenClaimNames.AT_HASH,
            IdTokenClaimNames.C_HASH);

    public OAuth2AuthenticationTokenJwtCustomizerHandler(JwtCustomizerHandler jwtCustomizerHandler) {
        super(jwtCustomizerHandler);
    }

    @Override
    protected void customizeJwt(JwtEncodingContext jwtEncodingContext) {

        Authentication authentication = jwtEncodingContext.getPrincipal();

        Map<String, Object> thirdPartyClaims = extractClaims(authentication);

        JwtClaimsSet.Builder jwtClaimSetBuilder = jwtEncodingContext.getClaims();

        jwtClaimSetBuilder.claims(existingClaims -> {

            // Remove conflicting claims set by this authorization server
            existingClaims.keySet().forEach(thirdPartyClaims::remove);

            // Remove standard id_token claims that could cause problems with clients
            ID_TOKEN_CLAIMS.forEach(thirdPartyClaims::remove);

            // Add all other claims directly to id_token
            existingClaims.putAll(thirdPartyClaims);
        });

    }

    private Map<String, Object> extractClaims(Authentication authentication) {
        Map<String, Object> claims;
        Object principalObj = authentication.getPrincipal();
        if (principalObj instanceof OidcUser oidcUser) {
            OidcIdToken idToken = oidcUser.getIdToken();
            claims = idToken.getClaims();
        } else if (principalObj instanceof OAuth2User oauth2User) {
            claims = oauth2User.getAttributes();
        } else {
            claims = Collections.emptyMap();
        }

        return new HashMap<>(claims);
    }

    @Override
    protected boolean supportCustomizeContext(Authentication authentication) {
        return authentication != null && authentication instanceof OAuth2AuthenticationToken;
    }

}
