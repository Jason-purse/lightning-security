package com.generatera.security.authorization.server.specification.components.token.format.jwt;

import com.generatera.security.authorization.server.specification.LightningUserPrincipal;
import com.generatera.security.authorization.server.specification.components.token.SignatureAlgorithm;
import com.generatera.security.authorization.server.specification.TokenIssueFormat;
import com.generatera.security.authorization.server.specification.components.token.LightningTokenContext;
import com.generatera.security.authorization.server.specification.components.token.LightningTokenType;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.customizer.LightningJwtCustomizer;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.jose.JwsHeader;
import org.springframework.security.core.GrantedAuthority;
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
public class DefaultLightningJwtGenerator implements LightningJwtGenerator {

    private final LightningJwtEncoder jwtEncoder;

    /**
     * lightning jwt customizer ...
     */
    private LightningJwtCustomizer jwtCustomizer;

    private final Boolean isOpaque;

    public DefaultLightningJwtGenerator(LightningJwtEncoder jwtEncoder) {
       this(jwtEncoder,false);
    }

    public DefaultLightningJwtGenerator(LightningJwtEncoder jwtEncoder,Boolean isOpaque) {
        Assert.notNull(jwtEncoder, "jwtEncoder cannot be null");
        Assert.notNull(isOpaque,"isOpaque cannot be null");
        this.jwtEncoder = jwtEncoder;
        this.isOpaque = false;
    }

    @Override
    public LightningJwt generate(LightningTokenContext context) {
        if (!TokenIssueFormat.SELF_CONTAINED.equals(context.getTokenSettings().getAccessTokenIssueFormat())) {
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

            LightningUserPrincipal principal = context.getPrincipal();
            claimsBuilder
                    .subject(principal.getUsername())
                    .audience(context.getTokenSettings().getAudiences())
                    .issuedAt(issuedAt)
                    .expiresAt(expiresAt);
            // 访问Token
            if (context.getTokenType() == LightningTokenType.LightningAuthenticationTokenType.ACCESS_TOKEN_TYPE) {
                claimsBuilder.notBefore(issuedAt);
                if(!isOpaque) {
                    if (!CollectionUtils.isEmpty(principal.getAuthorities())) {
                        claimsBuilder.claim("scope", org.apache.commons.lang3.StringUtils.joinWith(",",principal.getAuthorities().stream().map(GrantedAuthority::getAuthority).toArray(Object[]::new)));
                    }
                }
            } else {
                // 刷新token 配置
            }

            claimsBuilder.claim("isOpaque",isOpaque);

            // 加密算法可以定制 ...
            JwsHeader.Builder headersBuilder = JwsHeader.with(SignatureAlgorithm.RS256);
            if (this.jwtCustomizer != null) {
                JwtEncodingContext.Builder jwtContextBuilder
                        = JwtEncodingContext.with(headersBuilder, claimsBuilder)
                        // 直接尝试 context 复制即可 ..
                        .context(contextMap -> contextMap.putAll(context.getContexts()));

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
