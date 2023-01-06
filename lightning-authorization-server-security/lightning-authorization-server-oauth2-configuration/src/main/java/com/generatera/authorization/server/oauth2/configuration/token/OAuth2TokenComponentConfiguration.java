package com.generatera.authorization.server.oauth2.configuration.token;

import com.generatera.authorization.server.oauth2.configuration.token.claims.OAuth2TokenClaimsCustomizer;
import com.generatera.authorization.server.oauth2.configuration.token.claims.OAuth2TokenClaimsCustomizerImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenClaimsContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

/**
 * 加入 token 生成器 和 token 解析器
 */
@Configuration
public class OAuth2TokenComponentConfiguration {


    //@Bean
    //public OAuth2TokenCustomizer<JwtEncodingContext> buildJwtCustomizer() {
    //
    //    LightningJwtCustomizerHandler jwtCustomizerHandler = OAuth2JwtCustomizerHandler.getJwtCustomizerHandler();
    //    LightningJwtCustomizer jwtCustomizer = new JwtCustomizerImpl(jwtCustomizerHandler);
    //
    //    return (context) -> {
    //
    //        JwsHeader jwsHeader = context.getHeaders().build();
    //        JwtClaimsSet claimsSet = context.getClaims().build();
    //
    //
    //        // 丑陋的代码
    //        com.generatera.authorization.server.common.configuration.token.customizer.jwt.
    //                JwtEncodingContext encodingContext
    //                = com.generatera.authorization.server.common.configuration.token.customizer.jwt.
    //                JwtEncodingContext
    //                .with(
    //                        com.generatera.authorization.server.common.configuration.token.customizer.jwt.jose.
    //                                JwsHeader.from(jwsHeader.getHeaders()
    //                        ),
    //                        com.generatera.authorization.server.common.configuration.token.customizer.jwt.
    //                                JwtClaimsSet
    //                                .from(claimsSet.getClaims())
    //                ).build();
    //
    //
    //        jwtCustomizer.customizeToken(encodingContext);
    //    };
    //}

    @Bean
    public OAuth2TokenCustomizer<OAuth2TokenClaimsContext> buildOAuth2TokenClaimsCustomizer() {

        OAuth2TokenClaimsCustomizer oauth2TokenClaimsCustomizer = new OAuth2TokenClaimsCustomizerImpl();

        return oauth2TokenClaimsCustomizer::customizeTokenClaims;
    }

}
