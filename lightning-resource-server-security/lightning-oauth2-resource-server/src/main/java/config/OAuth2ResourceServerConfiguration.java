package config;


import com.generatera.resource.server.config.LightningResourceServerConfig;
import com.generatera.resource.server.config.LightningResourceServerConfigurer;
import com.generatera.resource.server.config.LogUtil;
import com.generatera.resource.server.config.ResourceServerProperties;
import com.generatera.resource.server.config.ResourceServerProperties.TokenVerificationConfig;
import com.generatera.security.authorization.server.specification.ProviderExtUtils;
import com.jianyue.lightning.boot.starter.util.ElvisUtil;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import config.token.LightningAuthenticationTokenResolver;
import config.token.jose.NimbusJwtDecoderExtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.web.HeaderBearerTokenResolver;

import java.util.function.Supplier;

/**
 * token 解析注册 ...
 * 1. jwt
 * 1.1 bearer token
 * 1.2 opaque token
 * 需要的依赖 spring-security-oauth2-jose
 * <p>
 * 当oauth2 authorization server(这里指的是非 central) 本身就是 resource server时,我们需要做额外的处理 ..
 * 这种情况下, oauth2 authorization client server 会提供ProviderSettings等共有的信息 ..
 */
@Slf4j
@EnableConfigurationProperties(OAuth2ResourceServerProperties.class)
@AutoConfigureAfter(LightningResourceServerConfig.class)
@Import(OAuth2ResourceServerComponentsImportSelector.class)
public class OAuth2ResourceServerConfiguration {

    @Bean
    public LightningResourceServerConfigurer oauth2ResourceServerConfigurer(
            ResourceServerProperties properties,
            org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties jwtProperties) {
        return new LightningResourceServerConfigurer() {
            @Override
            public void configure(OAuth2ResourceServerConfigurer<HttpSecurity> resourceServerConfigurer) throws Exception {
                TokenVerificationConfig tokenVerificationConfig = properties.getTokenVerificationConfig();
                if (tokenVerificationConfig.getTokenType() == TokenVerificationConfig.TokenType.Bearer) {
                    OAuth2ResourceServerConfigurer<HttpSecurity>.JwtConfigurer jwtConfigurer = resourceServerConfigurer.jwt();

                    // 存在 jwt Source(存在授权服务器配置) ...
                    // 否则 根据spring.oauth2.resource.server.jwkSetUrl 进行配置也可以 ..
                    JWKSource<SecurityContext> jwkSource = ProviderExtUtils.getJwkSource(resourceServerConfigurer.and());
                    if (jwkSource != null) {
                        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoderExtUtils.fromJwkSource(jwkSource, jwtProperties.getJwt());
                        jwtConfigurer.decoder(jwtDecoder);
                    }

                    ElvisUtil.isNotEmptyConsumer(tokenVerificationConfig.getBearerTokenConfig().getNeedPrefix(),
                            status -> {
                                if (!status) {
                                    // header 直接解析
                                    resourceServerConfigurer.bearerTokenResolver(
                                            new HeaderBearerTokenResolver(LightningAuthenticationTokenResolver.TOKEN_IDENTITY_NAME));
                                }
                            });
                } else {
                    resourceServerConfigurer.opaqueToken();
                }

                LogUtil.prettyLog("oauth2 resource server enabled !!!!");
            }
        };
    }
}
