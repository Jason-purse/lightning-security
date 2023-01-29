package com.generatera.security.authorization.server.specification.components.token.format.jwt;

import com.generatera.security.authorization.server.specification.LightningUserPrincipal;
import com.generatera.security.authorization.server.specification.TokenIssueFormat;
import com.generatera.security.authorization.server.specification.components.token.LightningTokenContext;
import com.generatera.security.authorization.server.specification.components.token.LightningTokenCustomizer;
import com.generatera.security.authorization.server.specification.components.token.LightningTokenType;
import com.generatera.security.authorization.server.specification.components.token.SignatureAlgorithm;
import com.generatera.security.authorization.server.specification.components.token.format.JwtExtClaimNames;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.jose.JwsHeader;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

/**
 * @author FLJ
 * @date 2023/1/6
 * @time 14:12
 * @Description 默认实现 JWT的 生成器
 */
public class DefaultLightningJwtGenerator implements LightningJwtGenerator {

    private final LightningJwtEncoder jwtEncoder;

    private List<String> authoritiesName = Arrays.asList(JwtExtClaimNames.SCOPE_CLAIM,JwtExtClaimNames.SCOPE_SHORT_CLAIM);

    public void setAuthoritiesName(List<String> authoritiesName) {
        Assert.notNull(authoritiesName,"authoritiesName must not be null !!!");
        this.authoritiesName = authoritiesName;
    }

    /**
     * lightning jwt customizer ...
     */
    private LightningTokenCustomizer<JwtEncodingContext> jwtCustomizer;

    public DefaultLightningJwtGenerator(LightningJwtEncoder jwtEncoder) {
        Assert.notNull(jwtEncoder, "jwtEncoder cannot be null");
        this.jwtEncoder = jwtEncoder;
    }

    @Override
    public LightningJwt generate(LightningTokenContext context) {
        if(context.getTokenType() != null && (LightningTokenType.LightningAuthenticationTokenType.ACCESS_TOKEN_TYPE.value().equals(context.getTokenType().value()))) {
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
                    if (!CollectionUtils.isEmpty(principal.getAuthorities())) {
                        // 取第一个 ...
                        claimsBuilder.claim(authoritiesName.get(0), org.apache.commons.lang3.StringUtils.joinWith(" ",principal.getAuthorities().stream().map(GrantedAuthority::getAuthority).toArray(Object[]::new)));
                    }
                } else {
                    // oidc 处理 ...
                    // 但是没有,所以不需要 ..
                    //claimsBuilder.claim("azp", registeredClient.getClientId());
                    //if (AuthorizationGrantType.AUTHORIZATION_CODE.equals(context.getAuthorizationGrantType())) {
                    //    OAuth2AuthorizationRequest authorizationRequest = (OAuth2AuthorizationRequest)context.getAuthorization().getAttribute(OAuth2AuthorizationRequest.class.getName());
                    //    String nonce = (String)authorizationRequest.getAdditionalParameters().get("nonce");
                    //    if (StringUtils.hasText(nonce)) {
                    //        claimsBuilder.claim("nonce", nonce);
                    //    }
                    //}
                }

                // 加密算法可以定制 ...
                JwsHeader.Builder headersBuilder = JwsHeader.with(SignatureAlgorithm.RS256);
                if (this.jwtCustomizer != null) {
                    JwtEncodingContext.Builder jwtContextBuilder
                            = JwtEncodingContext.with(headersBuilder, claimsBuilder)
                            // 直接尝试 context 复制即可 ..
                            .context(contextMap -> contextMap.putAll(context.getContexts()));

                    JwtEncodingContext jwtContext = jwtContextBuilder.build();
                    this.jwtCustomizer.customize(jwtContext);
                }

                JwsHeader headers = headersBuilder.build();
                JwtClaimsSet claims = claimsBuilder.build();
                return this.jwtEncoder.encode(JwtEncoderParameters.from(headers, claims, context.getTokenType(), context.getTokenValueType()));
            }
        }
        return null;
    }

    public void setJwtCustomizer(LightningTokenCustomizer<JwtEncodingContext> jwtCustomizer) {
        Assert.notNull(jwtCustomizer, "jwtCustomizer cannot be null");
        this.jwtCustomizer = jwtCustomizer;
    }
}
