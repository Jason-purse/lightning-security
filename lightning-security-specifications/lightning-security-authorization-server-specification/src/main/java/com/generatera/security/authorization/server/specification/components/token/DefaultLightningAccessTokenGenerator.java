package com.generatera.security.authorization.server.specification.components.token;

import com.generatera.security.authorization.server.specification.TokenIssueFormat;
import com.generatera.security.authorization.server.specification.components.token.LightningToken.LightningAccessToken;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.ClaimAccessor;
import com.generatera.security.authorization.server.specification.components.token.format.plain.DefaultPlainToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.keygen.Base64StringKeyGenerator;
import org.springframework.security.crypto.keygen.StringKeyGenerator;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    private LightningTokenCustomizer<LightningTokenClaimsContext> accessTokenCustomizer;


    public DefaultLightningAccessTokenGenerator() {
        this(false);
    }

    public DefaultLightningAccessTokenGenerator(boolean isOpaque) {
        this(isOpaque, "scope");
    }

    public DefaultLightningAccessTokenGenerator(boolean isOpaque, String authoritiesName) {
        this.isOpaque = isOpaque;
        this.authoritiesName = authoritiesName;
    }


    @Override
    public LightningAuthenticationAccessToken generate(LightningTokenContext context) {

        // 访问 token 且 reference 形式才生成 token
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

            if (!isOpaque) {
                if (!CollectionUtils.isEmpty(context.getPrincipal().getAuthorities())) {
                    claimsBuilder.claim(authoritiesName, context.getPrincipal().getAuthorities().stream().map(GrantedAuthority::getAuthority).toArray(Object[]::new));
                }
            }

            // 是否为 不透明token ..
            claimsBuilder.claim("isOpaque", isOpaque);


            // 访问 token 自定义器 ...
            if (this.accessTokenCustomizer != null) {
                LightningTokenClaimsContext.Builder accessTokenContextBuilder =
                        LightningTokenClaimsContext.with(claimsBuilder)
                                // 直接尝试contexts 复制即可 ..
                                .context(contextMap -> contextMap.putAll(context.getContexts()));

                LightningTokenClaimsContext accessTokenContext = accessTokenContextBuilder.build();
                this.accessTokenCustomizer.customize(accessTokenContext);
            }

            LightningTokenClaimsSet accessTokenClaimsSet = claimsBuilder.build();
            return new LightningAccessTokenClaims(
                    context.getTokenValueType(),
                    context.getTokenSettings().getAccessTokenValueFormat(),
                    this.accessTokenGenerator.generateKey(), accessTokenClaimsSet.getIssuedAt(),
                    accessTokenClaimsSet.getExpiresAt(),
                    context.getPrincipal().getAuthorities().stream().map(GrantedAuthority::getAuthority).toList(),
                    accessTokenClaimsSet.getClaims()
            );
        }

        return null;
    }


    public void setAccessTokenCustomizer(LightningTokenCustomizer<LightningTokenClaimsContext> accessTokenCustomizer) {
        Assert.notNull(accessTokenCustomizer, "accessTokenCustomizer must not be null !!!");
        this.accessTokenCustomizer = accessTokenCustomizer;
    }

    private static final class LightningAccessTokenClaims extends LightningAuthenticationAccessToken implements LightningAccessToken, ClaimAccessor {
        private final Map<String, Object> claims;

        private final List<String> scopes;

        private LightningAccessTokenClaims(LightningTokenType.LightningTokenValueType tokenValueType,
                                           LightningTokenType.LightningTokenValueFormat tokenValueFormat,
                                           String tokenValue,
                                           Instant issuedAt, Instant expiresAt,
                                           List<String> scopes,
                                           Map<String, Object> claims) {
            super(new DefaultPlainToken(tokenValue, issuedAt, expiresAt), tokenValueType, tokenValueFormat);
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
