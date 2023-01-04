package com.generatera.authorization.application.server.form.login.config.token;

import com.generatera.authorization.server.common.configuration.token.customizer.jwt.JwtEncodingContext;
import com.generatera.authorization.server.common.configuration.token.LightningToken;
import com.generatera.authorization.server.common.configuration.token.LightningUserPrincipal;
import com.generatera.authorization.server.common.configuration.token.OAuthTokenFormat;
import com.generatera.authorization.server.common.configuration.token.customizer.jwt.*;
import com.generatera.authorization.server.common.configuration.token.customizer.jwt.jose.JwsHeader;
import org.jetbrains.annotations.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Instant;
/**
 * @author FLJ
 * @date 2023/1/3
 * @time 13:18
 * @Description 表单登录的 Jwt Generator ..
 */
public final class FormLoginJwtGenerator implements LightningJwtGenerator<FormLoginSecurityContext> {

    private final LightningJwtEncoder jwtEncoder;

    private LightningJwtCustomizer jwtCustomizer;

    public FormLoginJwtGenerator(LightningJwtEncoder jwtEncoder) {
        Assert.notNull(jwtEncoder, "jwtEncoder cannot be null");
        this.jwtEncoder = jwtEncoder;
    }

    @Nullable
    public LightningJwt generate(FormLoginSecurityContext context) {

        if (!OAuthTokenFormat.SELF_CONTAINED.equals(context.getTokenSettings().getAccessTokenFormat())) {
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
            if (context.getTokenType() == LightningToken.TokenType.ACCESS_TOKEN_TYPE) {
                claimsBuilder.notBefore(issuedAt);
                if (!CollectionUtils.isEmpty(principal.getAuthoritiesForString())) {
                    claimsBuilder.claim("scope", principal.getAuthoritiesForString());
                }
            }
            else {
                // 刷新token 配置
            }

            JwsHeader.Builder headersBuilder = JwsHeader.with(SignatureAlgorithm.RS256);
            if (this.jwtCustomizer != null) {
                JwtEncodingContext.Builder jwtContextBuilder
                        = JwtEncodingContext.with(headersBuilder, claimsBuilder)
                .principal(context.getAuthentication())
                .providerContext(context.getProviderContext())
                //.authorizedScopes(principal.getAuthorities())
                .tokenType(context.getTokenType());

                JwtEncodingContext jwtContext = jwtContextBuilder.build();
                this.jwtCustomizer.customizeToken(jwtContext);
            }

            JwsHeader headers = headersBuilder.build();
            JwtClaimsSet claims = claimsBuilder.build();
            return this.jwtEncoder.encode(JwtEncoderParameters.from(headers, claims, context.getTokenType()));
        }
    }

    public void setJwtCustomizer(LightningJwtCustomizer jwtCustomizer) {
        Assert.notNull(jwtCustomizer, "jwtCustomizer cannot be null");
        this.jwtCustomizer = jwtCustomizer;
    }
}