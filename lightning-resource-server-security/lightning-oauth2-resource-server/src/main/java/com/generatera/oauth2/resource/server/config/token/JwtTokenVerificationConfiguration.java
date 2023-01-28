package com.generatera.oauth2.resource.server.config.token;

import com.generatera.oauth2.resource.server.config.token.jose.NimbusJwtDecoderExtUtils;
import com.generatera.resource.server.config.LightningResourceServerConfigurer;
import com.generatera.resource.server.config.LogUtil;
import com.generatera.resource.server.config.ResourceServerProperties;
import com.generatera.security.authorization.server.specification.components.token.JwtClaimsToUserPrincipalMapper;
import com.generatera.security.authorization.server.specification.ProviderExtUtils;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.util.Collection;

/**
 * @author FLJ
 * @date 2023/1/12
 * @time 11:24
 * @Description 用于解析 bearer token 中的 jwt
 */
@AutoConfiguration
@Configuration
public class JwtTokenVerificationConfiguration {

    /**
     * 一般来说,应用都需要自定义自己的转换器 ..
     */
    @Bean
    @ConditionalOnMissingBean(LightningJwtAuthenticationConverter.class)
    public PlainJwtAuthenticationConverter jwtAuthenticationConverter(
            @Autowired(required = false)
                    JwtClaimsToUserPrincipalMapper principalMapper,
            @Autowired(required = false)
                    Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter
    ) {
        PlainJwtAuthenticationConverter lightningJwtAuthenticationConverter = new PlainJwtAuthenticationConverter();
        // grantedAuthorities handle
        if (jwtGrantedAuthoritiesConverter != null) {
            lightningJwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        }
        lightningJwtAuthenticationConverter.setJwtClaimsMapper(principalMapper);
        return lightningJwtAuthenticationConverter;
    }

    @Bean
    public LightningResourceServerConfigurer resourceServerConfigurer(
            OAuth2ResourceServerProperties oAuth2ResourceServerProperties,
            ResourceServerProperties properties,
            LightningJwtAuthenticationConverter authenticationConverter
    ) {
        return new LightningResourceServerConfigurer() {
            @Override
            public void configure(HttpSecurity security) throws Exception {
                OAuth2ResourceServerConfigurer<HttpSecurity> configurer = security.oauth2ResourceServer();
                OAuth2ResourceServerConfigurer<HttpSecurity>.JwtConfigurer jwtConfigurer = configurer.jwt();

                // 存在 jwt Source(存在授权服务器配置) ...
                // 否则 根据spring.oauth2.resource.server.jwkSetUrl 进行配置也可以 ..
                JWKSource<SecurityContext> jwkSource = ProviderExtUtils.getJwkSource(security);
                if (jwkSource != null) {
                    NimbusJwtDecoder jwtDecoder = NimbusJwtDecoderExtUtils.fromJwkSource(jwkSource, oAuth2ResourceServerProperties.getJwt());
                    jwtConfigurer.decoder(jwtDecoder);
                }

                // 转换器, 转换到兼容 LightningUserPrincipal的状态下 ..
                jwtConfigurer.jwtAuthenticationConverter(authenticationConverter);

                LogUtil.prettyLog("oauth2 jwt token resource server enable !!!");
            }
        };
    }

}
