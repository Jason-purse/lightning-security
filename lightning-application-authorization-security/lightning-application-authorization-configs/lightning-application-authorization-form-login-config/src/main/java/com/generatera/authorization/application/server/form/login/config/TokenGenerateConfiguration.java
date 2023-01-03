//package com.generatera.authorization.application.server.form.login.config;
//
//import com.generatera.authorization.application.server.form.login.config.token.DefaultFormLoginAuthenticationTokenGenerator;
//import com.generatera.authorization.application.server.form.login.config.token.FormLoginAuthenticationTokenGenerator;
//import com.generatera.authorization.application.server.form.login.config.token.LightningFormLoginAccessTokenGenerator;
//import com.generatera.authorization.application.server.form.login.config.token.LightningFormLoginRefreshTokenGenerator;
//import com.generatera.authorization.server.common.configuration.AuthorizationServerComponentProperties;
//import com.generatera.authorization.server.common.configuration.token.*;
//import com.nimbusds.jose.jwk.source.JWKSource;
//import com.nimbusds.jose.proc.SecurityContext;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
//import org.springframework.context.annotation.Bean;
//
//import static com.generatera.authorization.server.common.configuration.AuthorizationServerComponentProperties.TOKEN_GENERATOR_NAME;
//
//public class TokenGenerateConfiguration {
//
//
//    public static class DelegateTokenGenerateConfiguration {
//        /**
//         * 当 容器中存在这个token 生成器 ...
//         * <p>
//         * 当oauth2 login 启用,但是容器中有自定义的 FormLoginAuthenticationTokenGenerator,那么 则不在生成 ..
//         * 因为用户已经知道了 要求 ...
//         * <p>
//         * 否则必然不存在FormLoginAuthenticationTokenGenerator ...
//         * <p>
//         * 但是 oauth2 login 到底有没有出现 ... 我们就不管了,直接引用这个Token 生成器进行处理即可 ...
//         * 有可能它
//         */
//        @Bean
//        public FormLoginAuthenticationTokenGenerator formLoginTokenGenerator(
//                @Autowired(required = false) @Qualifier(TOKEN_GENERATOR_NAME)
//                        LightningAuthenticationTokenGenerator authenticationTokenGenerator,
//                @Autowired(required = false)
//                        FormLoginAuthenticationTokenGenerator formLoginAuthenticationTokenGenerator
//        ) {
//            if (authenticationTokenGenerator != null) {
//                return authenticationTokenGenerator::generate;
//            }
//            else {
//                // 否则表示也只有 FormLogin
//            }
//
//
//        }
//    }
//
//    /**
//     * 根据 ioc容器中是否存在  表单访问 Token生成器 / 或者 RefreshToken生成器来创建新的 ..
//     */
//    public static class DefaultTokenGenerateConfiguration {
//        /**
//         * 只存在 Form Login 时 ..
//         *
//         * @param properties            auth server properties
//         * @param jwkSource             jwtSource
//         * @param accessTokenGenerator  accessTokenGenerator ..
//         * @param refreshTokenGenerator refreshTokenGenerator ...
//         * @return
//         */
//        @Bean
//        @ConditionalOnMissingBean(LightningAuthenticationTokenGenerator.class)
//        public FormLoginAuthenticationTokenGenerator formLoginTokenGenerator(AuthorizationServerComponentProperties properties,
//                                                                             JWKSource<SecurityContext> jwkSource,
//                                                                             @Autowired(required = false)
//                                                                                     LightningFormLoginAccessTokenGenerator accessTokenGenerator,
//                                                                             @Autowired(required = false)
//                                                                                     LightningFormLoginRefreshTokenGenerator refreshTokenGenerator) {
//
//
//            AuthorizationServerComponentProperties.TokenSettings tokenSettings = properties.getTokenSettings();
//            Boolean isPlain = tokenSettings.getIsPlain();
//            isPlain = isPlain != null ? isPlain : Boolean.FALSE;
//            if (accessTokenGenerator != null || refreshTokenGenerator != null) {
//
//                // 生成 DefaultFormLogin AuthenticationTokenGenerator
//                DefaultFormLoginAuthenticationTokenGenerator defaultFormLoginAuthenticationTokenGenerator = new DefaultFormLoginAuthenticationTokenGenerator(jwkSource);
//
//                tokenSettings.setIsPlain(isPlain);
//
//                if (accessTokenGenerator != null) {
//                    defaultFormLoginAuthenticationTokenGenerator.setAccessTokenGenerator(accessTokenGenerator);
//                }
//                if (refreshTokenGenerator != null) {
//                    defaultFormLoginAuthenticationTokenGenerator.setRefreshTokenGenerator(refreshTokenGenerator);
//                }
//
//                return defaultFormLoginAuthenticationTokenGenerator;
//
//            } else {
//                Boolean finalIsPlain = isPlain;
//
//                // 否则直接使用默认 AuthServer Common Component 提供的 ..
//                // 形成代理 ...
//                return new FormLoginAuthenticationTokenGenerator() {
//                    private final DelegateLightningAuthenticationTokenGenerator tokenGenerator =
//                            new DelegateLightningAuthenticationTokenGenerator(new DefaultAuthenticationTokenGenerator(finalIsPlain, jwkSource));
//
//                    @Override
//                    public LightningAuthenticationToken generate(LightningAuthenticationSecurityContext securityContext) {
//                        return tokenGenerator.generate(securityContext);
//                    }
//                };
//            }
//        }
//    }
//}
