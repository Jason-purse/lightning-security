package com.generatera.oauth2.resource.server.config.token;

import com.generatera.security.authorization.server.specification.ClientAuthenticationMethod;
import com.generatera.security.authorization.server.specification.HandlerFactory;
import com.generatera.oauth2.resource.server.config.OAuth2ResourceServerProperties;
import com.generatera.resource.server.config.LightningResourceServerConfigurer;
import com.generatera.resource.server.config.LogUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.util.Assert;

import java.util.List;

/**
 * @author FLJ
 * @date 2023/1/12
 * @time 14:13
 * @Description 对于 opaque token 校验,通常会通过 OAuth 2.0 Introspection Endpoint 校验 ..
 * 也就是说 授权中心必须高可用 ...(因为token 校验端点存在于 授权服务器中) ...
 * 这能够进行token撤销 ...
 * <p>
 * 当作为一个 opaque token resource server时,我们需要配置对应的 需要请求的introspection endpoint ..
 * 并且需要包含client-id and client-secret 信息 ...(用来向授权服务器发起请求) ...
 * <p>
 * <p>
 * 处理流程是:
 * 1. 发起token 校验请求
 * 2. Inspect the response for an { 'active' : true } attribute
 * 3. Map each scope to an authority with the prefix SCOPE_
 * <p>
 * 默认 Authentication#getPrincipal,是一个 OAuth2AuthenticatedPrincipal 对象,但是我们需要做除了oauth2的其他兼容,所以它最终应该是一个
 * {@link com.generatera.security.authorization.server.specification.LightningUserPrincipal}
 * <p>
 * Authentication#getName maps to the token’s sub property, if one is present.
 * <p>
 * <p>
 * 资源服务器的token 处理流程:
 * 1. 通过bearer token 请求解析器解析 token str
 * 2. 通过  OpaqueTokenAuthenticationProvider 校验 token
 * 会使用  OpaqueTokenIntrospector 进行 opaqueToken 验证 ..
 * 验证成功之后会返回一个  BearerTokenAuthentication
 */
@AutoConfiguration
public class OpaqueTokenVerificationConfiguration {

    // 客户端 认证方法的处理器提供者
    interface ClientAuthenticationMethodHandlerProvider extends HandlerFactory.HandlerProvider {
        @Override
        default Object key() {
            return LightningOAuth2OpaqueTokenIntrospector.class;
        }
    }

    interface ClientAuthenticationMethodHandler extends HandlerFactory.Handler {

        LightningOAuth2OpaqueTokenIntrospector getTokenIntrospector(OAuth2ResourceServerProperties properties);
    }

    static {
        HandlerFactory.registerHandler(
                new ClientAuthenticationMethodHandlerProvider() {
                    @Override
                    public boolean support(Object predicate) {
                        return ClientAuthenticationMethod.BASIC.getValue().equalsIgnoreCase(predicate.toString());
                    }

                    @NotNull
                    @Override
                    public HandlerFactory.Handler getHandler() {
                        return (ClientAuthenticationMethodHandler) properties -> DefaultOpaqueTokenIntrospector.clientSecretBasicOf(
                                properties.getOpaqueTokenConfig().getIntrospectTokenEndpointUrl(),
                                properties.getOpaqueTokenConfig().getClientId(),
                                properties.getOpaqueTokenConfig().getClientSecret()
                        );
                    }
                }
        );

        HandlerFactory.registerHandler(
                new ClientAuthenticationMethodHandlerProvider() {
                    @Override
                    public boolean support(Object predicate) {
                        return ClientAuthenticationMethod.POST.getValue().equalsIgnoreCase(predicate.toString());
                    }

                    @NotNull
                    @Override
                    public HandlerFactory.Handler getHandler() {
                        return (ClientAuthenticationMethodHandler) properties -> DefaultOpaqueTokenIntrospector.clientSecretPostOf(
                                properties.getOpaqueTokenConfig().getIntrospectTokenEndpointUrl(),
                                properties.getOpaqueTokenConfig().getClientId(),
                                properties.getOpaqueTokenConfig().getClientSecret()
                        );
                    }
                }
        );
    }


    /**
     * 进行 opaque Token 省查器 ...
     * <p>
     * 否则用户可以重写自定义的 ...
     * <p>
     * 重写为了兼容到 ... {@link com.generatera.oauth2.resource.server.config.LightningOAuth2UserPrincipal}
     */
    @Bean
    @ConditionalOnMissingBean(LightningOAuth2OpaqueTokenIntrospector.class)
    public LightningOAuth2OpaqueTokenIntrospector tokenIntrospector(OAuth2ResourceServerProperties properties) {

        List<String> clientMethods = properties.getOpaqueTokenConfig().getClientMethods();
        Assert.notNull(clientMethods, "clientMethods must not be null !!!");

        for (String clientMethod : clientMethods) {
            HandlerFactory.HandlerProvider handler = HandlerFactory.getHandler(LightningOAuth2OpaqueTokenIntrospector.class, clientMethod);
            if(handler != null) {
                return ((ClientAuthenticationMethodHandler) handler.getHandler()).getTokenIntrospector(properties);
            }
        }

        throw new UnsupportedOperationException("token introspector handlers must not be null !!!");
    }


    /**
     * 客户端凭证信息 需要提供 ...
     * <p>
     * opaque token 校验配置 ..
     */
    @Bean
    public LightningResourceServerConfigurer opaqueTokenResourceServerConfigurer(
            LightningOAuth2OpaqueTokenIntrospector tokenIntrospector
    ) {
        return new LightningResourceServerConfigurer() {
            @Override
            public void configure(HttpSecurity security) throws Exception {
                OAuth2ResourceServerConfigurer<HttpSecurity> configurer = security.oauth2ResourceServer();
                // 表示开启 opaqueToken
                OAuth2ResourceServerConfigurer<HttpSecurity>.OpaqueTokenConfigurer opaqueTokenConfigurer = configurer.opaqueToken();
                // override
                // 使用自定义的 认证提供器 ..
                opaqueTokenConfigurer.authenticationManager(
                        new ProviderManager(new DefaultLightningOpaqueTokenAuthenticationProvider(tokenIntrospector))
                );

                LogUtil.prettyLog(
                        "oauth2 opaque token resource server enable !!!"
                );
            }
        };
    }
}
