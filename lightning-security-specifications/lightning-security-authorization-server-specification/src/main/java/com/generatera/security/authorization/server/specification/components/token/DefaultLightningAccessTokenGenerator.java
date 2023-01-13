package com.generatera.security.authorization.server.specification.components.token;

import com.generatera.security.authorization.server.specification.TokenIssueFormat;
import com.generatera.security.authorization.server.specification.components.token.LightningToken.LightningAccessToken;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.ClaimAccessor;
import com.generatera.security.authorization.server.specification.components.token.format.plain.DefaultPlainToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.keygen.Base64StringKeyGenerator;
import org.springframework.security.crypto.keygen.StringKeyGenerator;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.*;

/**
 * @author FLJ
 * @date 2023/1/6
 * @time 13:51
 * @Description 默认的access token generator
 */
public class DefaultLightningAccessTokenGenerator implements LightningAccessTokenGenerator {
    private final StringKeyGenerator accessTokenGenerator = new Base64StringKeyGenerator(Base64.getUrlEncoder().withoutPadding(), 96);
    private final String authoritiesName;
    private final boolean isOpaque;

    public DefaultLightningAccessTokenGenerator() {
        this.isOpaque = false;
        this.authoritiesName  = "scope";
    }

    public DefaultLightningAccessTokenGenerator(boolean isOpaque,String authoritiesName) {
        this.isOpaque = isOpaque;
        this.authoritiesName = authoritiesName;
    }
    public DefaultLightningAccessTokenGenerator(boolean isOpaque) {
        this(isOpaque,"scope");
    }

    @Override
    public LightningAuthenticationAccessToken generate(LightningSecurityTokenContext context) {
        if (LightningTokenType.LightningAuthenticationTokenType.ACCESS_TOKEN_TYPE.equals(context.getTokenType())
                && TokenIssueFormat.REFERENCE.equals(context.getTokenSettings().getAccessTokenIssueFormat())) {
            String issuer = null;
            if (context.getProviderContext() != null) {
                issuer = context.getProviderContext().getIssuer();
            }

            Instant issuedAt = Instant.now();
            Instant expiresAt = issuedAt.plus(context.getTokenSettings().getAccessTokenTimeToLive());
            LightningTokenClaimsSet.Builder claimsBuilder = LightningTokenClaimsSet.builder();
            if (StringUtils.hasText(issuer)) {
                claimsBuilder.issuer(issuer);
            }

            claimsBuilder
                    .subject(context.getPrincipal().getName())
                    .audience(context.getTokenSettings().getAudiences())
                    .issuedAt(issuedAt).expiresAt(expiresAt).notBefore(issuedAt).id(UUID.randomUUID().toString());

            if(!isOpaque) {
                if (!CollectionUtils.isEmpty(context.getPrincipal().getAuthorities())) {
                    claimsBuilder.claim(authoritiesName, context.getPrincipal().getAuthorities().stream().map(GrantedAuthority::getAuthority).toArray(Object[]::new));
                }
            }

            // 是否为 不透明token ..
            claimsBuilder.claim("isOpaque",isOpaque);


            //if (this.accessTokenCustomizer != null) {
            //    LightningTokenClaimsContext.Builder accessTokenContextBuilder =
            //            LightningTokenClaimsSet.with(claimsBuilder).registeredClient(context.getRegisteredClient())).principal(context.getPrincipal())).providerContext(context.getProviderContext())).authorizedScopes(context.getAuthorizedScopes())).tokenType(context.getTokenType())).authorizationGrantType(context.getAuthorizationGrantType());
            //    if (context.getAuthorization() != null) {
            //        accessTokenContextBuilder.authorization(context.getAuthorization());
            //    }
            //
            //
            //    LightningTokenClaimsContext accessTokenContext = accessTokenContextBuilder.build();
            //    this.accessTokenCustomizer.customize(accessTokenContext);
            //}

            LightningTokenClaimsSet accessTokenClaimsSet = claimsBuilder.build();
            return new LightningAccessTokenClaims(
                    LightningTokenType.LightningTokenValueType.BEARER_TOKEN_TYPE,
                    this.accessTokenGenerator.generateKey(), accessTokenClaimsSet.getIssuedAt(),
                    accessTokenClaimsSet.getExpiresAt(),
                    context.getPrincipal().getAuthorities().stream().map(GrantedAuthority::getAuthority).toList(),
                    accessTokenClaimsSet.getClaims()
            );
        }

        return null;
    }


    private static final class LightningAccessTokenClaims extends LightningAuthenticationAccessToken implements LightningAccessToken, ClaimAccessor {
        private final Map<String, Object> claims;

        private final List<String> scopes;

        private LightningAccessTokenClaims(LightningTokenType.LightningTokenValueType tokenValueType, String tokenValue,
                                           Instant issuedAt, Instant expiresAt,
                                           List<String> scopes,
                                           Map<String, Object> claims) {
            super(new DefaultPlainToken(tokenValue, issuedAt, expiresAt), tokenValueType);
            this.claims = claims;
            this.scopes = scopes;
        }

        public Map<String, Object> getClaims() {
            return this.claims;
        }

        public List<String> getScopes() {
            return scopes;
        }
    }
}
