package com.generatera.security.server.token.specification.format.jwt;

import com.generatera.security.authorization.server.specification.LightningUserPrincipal;
import com.generatera.security.authorization.server.specification.SignatureAlgorithm;
import com.generatera.security.authorization.server.specification.TokenIssueFormat;
import com.generatera.security.server.token.specification.LightningAuthorizationServerTokenSecurityContext;
import com.generatera.security.server.token.specification.LightningTokenType;
import com.generatera.security.server.token.specification.format.jwt.customizer.JwtEncodingContext;
import com.generatera.security.server.token.specification.format.jwt.customizer.LightningJwtCustomizer;
import com.generatera.security.server.token.specification.format.jwt.jose.JwsHeader;
import com.generatera.security.server.token.specification.format.jwt.jose.NimbusJwtEncoder;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Instant;

/**
 * @author FLJ
 * @date 2023/1/6
 * @time 14:12
 * @Description 默认实现 JWT的 生成器
 */
public class DefaultLightningJwtGenerator implements LightningAuthorizationServerJwtGenerator {

    private final NimbusJwtEncoder jwtEncoder;

    private LightningJwtCustomizer jwtCustomizer;

    public DefaultLightningJwtGenerator(JWKSource<SecurityContext> jwkSource) {
        Assert.notNull(jwkSource, "jwkSource cannot be null");
        this.jwtEncoder = new NimbusJwtEncoder(jwkSource);
    }

    @Override
    public LightningJwt generate(LightningAuthorizationServerTokenSecurityContext context) {

        if (!TokenIssueFormat.SELF_CONTAINED.equals(context.getTokenSettings().getAccessTokenFormat())) {
            return null;
        } else {
            String issuer = null;
            if (context.getProviderContext() != null) {
                issuer = context.getProviderContext().getIssuer();
            }

            Instant issuedAt = Instant.now();
            Instant expiresAt = issuedAt.plus(context.getTokenSettings().getAccessTokenTimeToLive());

            JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder();
            if (StringUtils.hasText(issuer)) {
                claimsBuilder.issuer(issuer);
            }

            LightningUserPrincipal principal = (LightningUserPrincipal) context.getAuthentication().getPrincipal();
            claimsBuilder
                    .subject(principal.getName())
                    .audience(context.getTokenSettings().getAudiences())
                    .issuedAt(issuedAt)
                    .expiresAt(expiresAt);
            // 访问Token
            if (context.getTokenType() == LightningTokenType.LightningAuthenticationTokenType.ACCESS_TOKEN_TYPE) {
                claimsBuilder.notBefore(issuedAt);
                if (!CollectionUtils.isEmpty(principal.getAuthoritiesForString())) {
                    claimsBuilder.claim("scope", principal.getAuthoritiesForString());
                }
            } else {
                // 刷新token 配置
            }

            JwsHeader.Builder headersBuilder = JwsHeader.with(SignatureAlgorithm.RS256);
            if (this.jwtCustomizer != null) {
                JwtEncodingContext.Builder jwtContextBuilder
                        = JwtEncodingContext.with(headersBuilder, claimsBuilder)
                        .principal(context.getAuthentication())
                        .providerContext(context.getProviderContext())
                        //.authorizedScopes(principal.getAuthorities())
                        .tokenType(context.getTokenType())
                        .tokenValueType(context.getTokenValueType());

                JwtEncodingContext jwtContext = jwtContextBuilder.build();
                this.jwtCustomizer.customizeToken(jwtContext);
            }

            JwsHeader headers = headersBuilder.build();
            JwtClaimsSet claims = claimsBuilder.build();
            return this.jwtEncoder.encode(JwtEncoderParameters.from(headers, claims, context.getTokenType(), context.getTokenValueType()));
        }
    }

    public void setJwtCustomizer(LightningJwtCustomizer jwtCustomizer) {
        Assert.notNull(jwtCustomizer, "jwtCustomizer cannot be null");
        this.jwtCustomizer = jwtCustomizer;
    }
}
