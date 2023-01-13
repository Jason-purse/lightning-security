package com.generatera.oauth2.resource.server.config.token;

import com.generatera.oauth2.resource.server.config.LightningJwtOAuth2UserPrincipal;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.util.Assert;

import java.util.Collection;
/**
 * @author FLJ
 * @date 2023/1/12
 * @time 11:15
 * @Description Lightning Jwt authentication Converter
 *
 * 由于默认的 JWT 认证转换器  将 jwt 转换为了 LightningOAuth2UserPrincipal,可能拿取用户的一些信息不是很方便 ...
 * 默认不强制要求重写转换器或者 {@link com.generatera.security.authorization.server.specification.JwtClaimsToUserPrincipalMapper}
 * 但是 这仅仅是测试场景才如此 ...
 *
 * 另外token的生成是通过
 * {@link com.generatera.security.authorization.server.specification.components.token.format.jwt.LightningJwtGenerator}
 * {@link com.generatera.security.authorization.server.specification.components.token.LightningAccessTokenGenerator},
 * {@link com.generatera.security.authorization.server.specification.components.token.LightningRefreshTokenGenerator}
 *
 * 那么如果是Jwt,则需要遵守如下规则:
 *
 * 那么你有两种方式可以选择,第一种,通过 重写当前转换器 ..
 * 第二种,给出 {@link com.generatera.security.authorization.server.specification.JwtClaimsToUserPrincipalMapper}
 * 进行 jwt 到 UserPrincipal的映射 ...
 *
 *
 * 查看默认实现 {@link PlainJwtAuthenticationConverter} 了解使用详情 ...
 * @see com.generatera.security.authorization.server.specification.JwtClaimsToUserPrincipalMapper
 * @see com.generatera.authorization.application.server.oauth2.login.config.authority.LightningOAuth2UserService
 * @see com.generatera.authorization.application.server.oauth2.login.config.authority.LightningOidcUserService
 */
public class LightningJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    // 专门用来转换 权限的 ...
    private Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

    public LightningJwtAuthenticationConverter() {
    }

    public  AbstractAuthenticationToken convert(@NotNull Jwt jwt) {
        Collection<GrantedAuthority> authorities = this.extractAuthorities(jwt);
        return new UsernamePasswordAuthenticationToken(new LightningJwtOAuth2UserPrincipal(jwt), authorities);
    }

    /** @deprecated */
    @Deprecated
    protected Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        return this.jwtGrantedAuthoritiesConverter.convert(jwt);
    }

    public void setJwtGrantedAuthoritiesConverter(Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter) {
        Assert.notNull(jwtGrantedAuthoritiesConverter, "jwtGrantedAuthoritiesConverter cannot be null");
        this.jwtGrantedAuthoritiesConverter = jwtGrantedAuthoritiesConverter;
    }
}