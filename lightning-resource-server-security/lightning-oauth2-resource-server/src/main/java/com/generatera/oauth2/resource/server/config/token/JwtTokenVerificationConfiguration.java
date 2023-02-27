package com.generatera.oauth2.resource.server.config.token;

import com.generatera.oauth2.resource.server.config.OAuth2ResourceServerUtils;
import com.generatera.resource.server.config.LightningResourceServerConfigurer;
import com.generatera.resource.server.config.LogUtil;
import com.generatera.security.authorization.server.specification.components.token.JwtClaimsToUserPrincipalMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoder;

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
     * 使用默认的转换器,来将 oauth2 内容迁移到 lightning 相关的内容 ...
     */
    @Bean
    @Primary
    public PlainJwtAuthenticationConverter jwtAuthenticationConverter(
            @Autowired(required = false)
                    JwtClaimsToUserPrincipalMapper principalMapper,
            @Autowired(required = false)
                    LightningJwtGrantAuthorityMapper jwtGrantedAuthoritiesConverter
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
            LightningJwtAuthenticationConverter authenticationConverter
    ) {
        return new LightningResourceServerConfigurer() {
            @Override
            public void configure(HttpSecurity security) throws Exception {
                security.setSharedObject(OAuth2ResourceServerProperties.class, oAuth2ResourceServerProperties);
                OAuth2ResourceServerConfigurer<HttpSecurity> configurer = security.oauth2ResourceServer();
                OAuth2ResourceServerConfigurer<HttpSecurity>.JwtConfigurer jwtConfigurer = configurer.jwt();
                // 存在 jwt Source(存在授权服务器配置) ...
                // 当不存在的时候 ..
                // 否则 根据spring.oauth2.resource.server.jwkSetUrl 进行配置 ..
                JwtDecoder jwtDecoder = OAuth2ResourceServerUtils.jwtDecoder(security, oAuth2ResourceServerProperties);
                jwtConfigurer.decoder(jwtDecoder);

                // 转换器, 转换到兼容 LightningUserPrincipal的状态下 ..
                jwtConfigurer.jwtAuthenticationConverter(authenticationConverter);

                LogUtil.prettyLog("oauth2 jwt token resource server enable !!!");
            }
        };
    }

}
