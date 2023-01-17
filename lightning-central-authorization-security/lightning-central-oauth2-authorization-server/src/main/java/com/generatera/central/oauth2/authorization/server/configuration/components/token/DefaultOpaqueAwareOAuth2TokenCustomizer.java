package com.generatera.central.oauth2.authorization.server.configuration.components.token;

import com.generatera.security.authorization.server.specification.components.token.LightningTokenType;
import com.generatera.security.authorization.server.specification.components.token.format.JwtExtClaimNames;
import com.jianyue.lightning.boot.starter.generic.crud.service.support.ThreadLocalSupport;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2TokenType;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenClaimsContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenClaimsSet;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Consumer;

/**
 * @author FLJ
 * @date 2023/1/13
 * @time 13:14
 *
 * 默认使用 scope 作为 authorities ..
 * 但是可以定制 ..
 *
 * 如果定制返回authorities 为空,则默认使用scope的内容作为 authorities ..
 */
public class DefaultOpaqueAwareOAuth2TokenCustomizer implements LightningCentralOAuth2TokenCustomizer<OAuth2TokenContext> {

    private List<String> authoritiesName = Arrays.asList(JwtExtClaimNames.SCOPE_CLAIM,JwtExtClaimNames.SCOPE_SHORT_CLAIM);

    @Override
    public void customize(OAuth2TokenContext context) {

        if (context instanceof JwtEncodingContext jwtEncodingContext) {
            if (context.getTokenType() == OAuth2TokenType.ACCESS_TOKEN) {
                JwtClaimsSet.Builder claims = jwtEncodingContext.getClaims();
                claims.claims(claimsHandle());
            }

        } else if (context instanceof OAuth2TokenClaimsContext oAuth2TokenClaimsContext) {
            if (context.getTokenType() == OAuth2TokenType.ACCESS_TOKEN) {
                OAuth2TokenClaimsSet.Builder claims = oAuth2TokenClaimsContext.getClaims();
                claims.claims(claimsHandle());
            }
        }
        // pass
    }

    private List<? extends GrantedAuthority> extractAuthorities(OAuth2TokenContext context) {
        for (String authorityName : authoritiesName) {
            if(context.hasKey(authorityName)) {
                return context.get(authorityName);
            }
        }

        // 否则 默认使用 scope
        Set<String> authorizedScopes = context.getAuthorizedScopes();
        if(!CollectionUtils.isEmpty(authorizedScopes)) {
            return authorizedScopes.stream().map(SimpleGrantedAuthority::new).toList();
        }

        return Collections.emptyList();
    }

    public void setAuthoritiesName(List<String> authoritiesName) {
        Assert.notNull(authoritiesName,"authoritiesName must not be null !!!");
        this.authoritiesName = authoritiesName;
    }

    @NotNull
    private Consumer<Map<String, Object>> claimsHandle() {
        return claims ->
        {
            Object tokenValueTypeFormat = claims.get(JwtExtClaimNames.TOKEN_VALUE_FORMAT_CLAIM);
            Assert.notNull(tokenValueTypeFormat,"tokenValueTypeFormat must not be null !!!");
            if (tokenValueTypeFormat.toString().equalsIgnoreCase(LightningTokenType.LightningTokenValueFormat.OPAQUE.value())) {

                for (String authorityName : authoritiesName) {
                    Object scope = claims.get(authorityName);
                    if (scope != null) {
                        scopeThreadLocal.set(scope);
                        claims.remove(authorityName);
                        break;
                    }
                }

                claims.put(JwtExtClaimNames.OPAQUE_CLAIM, true);
            } else {
                claims.put(JwtExtClaimNames.OPAQUE_CLAIM, false);
            }
        };
    }

    /**
     * 和线程绑定相关 ...
     */
    public static ThreadLocalSupport<Object> scopeThreadLocal = ThreadLocalSupport.Companion.of();
}
