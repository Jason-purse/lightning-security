package com.generatera.oauth2.resource.server.config;

import com.generatera.security.authorization.server.specification.util.HttpSecurityBuilderUtils;
import com.generatera.oauth2.resource.server.config.token.jose.NimbusJwtDecoderExtUtils;
import com.generatera.security.authorization.server.specification.ProviderExtUtils;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.util.StringUtils;

public class OAuth2ResourceServerUtils {

    /**
     * 当资源服务器和 授权服务器共存的时候, 不管授权服务器是否分离配置,都能够根据 jwkSource 创建一个 jwt 解码器 ...
     *
     * 前提是,不存在 jwk 或者 issuer url的设置
     *
     * 否则 依据 jwk 或者 issuer url 配置处理 ...
     *
     * 如果最后都拿不到一个,则 抛出异常 ...
     */
    public static <B extends HttpSecurityBuilder<B>> JwtDecoder jwtDecoder(B builder, OAuth2ResourceServerProperties oAuth2ResourceServerProperties) {
        JwtDecoder jwtDecoder = builder.getSharedObject(JwtDecoder.class);
        if(jwtDecoder == null) {
            // 都存在的时候 ... 是可以的
            // 有可能属于其他  授权服务器的资源服务器 ...
            if(StringUtils.hasText(oAuth2ResourceServerProperties.getJwt().getJwkSetUri()) || StringUtils.hasText(oAuth2ResourceServerProperties.getJwt().getIssuerUri())) {
                // 直接从容器中获取
                jwtDecoder = HttpSecurityBuilderUtils.getBean(builder, JwtDecoder.class);
            }
            else {
                // 优化 jwkSet uri的形式
                JWKSource<SecurityContext> jwkSource = ProviderExtUtils.getJwkSource(builder);
                if(jwkSource != null) {
                    jwtDecoder = NimbusJwtDecoderExtUtils.fromJwkSource(jwkSource,oAuth2ResourceServerProperties.getJwt());
                }
                else {
                    // 否则必须提供一个 ...
                    jwtDecoder = HttpSecurityBuilderUtils.getBean(builder,JwtDecoder.class);
                }

            }
            builder.setSharedObject(JwtDecoder.class,jwtDecoder);
        }
        return jwtDecoder;
    }
}
