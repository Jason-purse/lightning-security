package com.generatera.central.authorization.server.configuration;

import com.generatera.authorization.server.common.configuration.AuthorizationServerCommonComponentsConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Configuration;

/**
 * @author FLJ
 * @date 2022/12/29
 * @time 14:48
 * @Description OAuth2 Server 基本配置
 *
 * spring auth2 server 为我们提供了许多可配置的选项,包括认证组件 ..
 *
 * 主要核心就是围绕 OAuth2AuthorizationServerConfigurer  定制OAuth2 authorization server的能力 ..
 *
 * 1. RegisteredClientRepository 管理新和存在的oauth2 客户端 ..
 *
 * 2. OAuth2AuthorizationService 管理新的和存在的 oauth2 authorization 信息(例如访问 token / 刷新token等)
 *
 * 3. OAuth2AuthorizationConsentService  管理新和存在的 授权协商(授权码授予流程需要)
 *
 * 4. ProviderSettings 定制Oauth2 authorization server的提供端元数据配置 ..
 *
 * 5. OAuth2TokenGenerator 由OAuth2 authorization server 用来支持生成token的 ..(默认只支持 jwt,如果是其他形式会抛出异常)
 *
 * 6. OAuth2ClientAuthenticationConfigurer  可以配置oauth2 client 认证
 *      1. AuthenticationConverter 尝试从HttpServletRequest中抓取客户端凭证信息到 OAuth2ClientAuthenticationToken .
 *      2. AuthenticationProvider  被用来认证 OAuth2ClientAuthenticationToken(可以使用委排模式 实现策略处理)
 *      3. AuthenticationSuccessHandler  客户端认证成功之后的成功处理并将OAuth2ClientAuthenticationToken  关联到SecurityContext ..
 *      4. AuthenticationFailureHandler  用来处理客户端认证失败,并返回OAuth2Error 响应
 *
 *      同样此配置器 会配置一个 OAuth2ClientAuthenticationFilter 并注册到 OAuth2 授权服务器的SecurityFilterChain @Bean中 ..
 *      也就是说上述的过滤器用来处理客户端认证请求 .
 *
 *      那么为什么需要客户端认证:
 *          1. oauth2 框架要求,访问OAuth2 Token端点,OAuth2 Token Introspection端点,OAuth2 Token Revocation端点都需要进行客户端认证 ..
 *          所以访问这些端点之前都会被客户但认证过滤器处理来进行成功认证之后才有资格访问 ..
 *      支持的客户端认证方式有:
 *          1. client_secret_basic
 *          2. client_secret_post
 *          3. private_key_jwt
 *          4. client_secret_jwt
 *          5. none(public client)
 *      目前此过滤器的默认配置是:
 *          1. authenticationConverter
 *              DelegatingAuthenticationConverter(JwtClientAssertionAuthenticationConverter, ClientSecretBasicAuthenticationConverter, ClientSecretPostAuthenticationConverter, and PublicClientAuthenticationConverter)
 *          2. authenticationProvider
 *              组合的(JwtClientAssertionAuthenticationProvider, ClientSecretAuthenticationProvider, and PublicClientAuthenticationProvider)
 *          3. authenticationSuccessHandler
 *              关联OAuth2ClientAuthenticationToken(authenticated)(标识当前已经认证的客户端) 到  SecurityContext
 *          4. errorResponseHandler
 *              关联OAuth2AuthenticationException 到 OAuth2Error去返回 OAuth2 error响应 ..
 *
 *
 *  7. OAuth2AuthorizationEndpointConfigurer
 *      主要提供了定制 OAuth2 授权端点的能力 ..主要是负责OAuth2 授权请求的 ..
 *      1. AuthenticationConverter  尝试从HttpServletRequest中抓取OAuth2 授权请求(或者协商)到  OAuth2AuthorizationCodeRequestAuthenticationToken
 *      2. AuthenticationProvider  被用来认证 OAuth2AuthorizationCodeRequestAuthenticationToken(委派,一般是)
 *      3. AuthenticationSuccessHandler  授权请求认证成功之后的 OAuth2AuthorizationCodeRequestAuthenticationToken 的处理并返回 OAuth2AuthorizationResponse ..
 *      4. AuthenticationFailureHandler  处理 OAuth2AuthorizationCodeRequestAuthenticationException 并返回 OAuth2Error 错误响应 ..
 *      5. consentPage 协商页面(自定义协商页面用来重定向资源拥有者-resource owner到此页面 - 如果当前处于授权请求流中)
 *      同样这个配置器会增加一个 OAuth2AuthorizationEndpointFilter 到authorization server的 SecurityFilterChain中 并处理 OAuth2 授权请求(以及协商)
 *
 *      当前此过滤器配置的默认值:
 *      1. authorizationRequestConverter -> AuthenticationConverter(OAuth2AuthorizationCodeRequestAuthenticationConverter)
 *      2. authenticationProvider -> AuthenticationManager(OAuth2AuthorizationCodeRequestAuthenticationProvider)
 *      3. authorizationResponseHandler -> AuthenticationSuccessHandler(处理 OAuth2AuthorizationCodeRequestAuthenticationToken 的内部实现并返回 OAuth2AuthorizationResponse 响应)
 *      4. errorResponseHandler -> AuthenticationFailureHandler(使用 OAuth2Error关联 OAuth2AuthorizationCodeRequestAuthenticationException并返回 OAuth2Error 响应)
 *
 *
 *  8. OAuth2TokenEndpointConfigurer 用来定制 OAuth2 Token 端点相关的信息
 *      1. AuthenticationConverter 用来从HttpServletRequest中抓取 OAuth2访问token请求到 OAuth2AuthorizationGrantAuthenticationToken 实例 ..
 *      2. AuthenticationProvider  用来认证 OAuth2AuthorizationGrantAuthenticationToken(委派)
 *      3. AuthenticationSuccessHandler  用来处理OAuth2AccessTokenAuthenticationToken 并返回 OAuth2AccessTokenResponse.
 *      4. errorResponseHandler 用来 处理OAuth2AuthenticationException 并返回一个OAuth2Error 响应 .
 *      除此之外,此配置器会配置一个 OAuth2TokenEndpointFilter 到oauth2 authorization server的SecurityFilterChain @Bean,OAuth2TokenEndpointFilter 用来处理 OAuth2 访问token 请求 ..
 *
 *      当前支持的授权授予类型是:
 *          1. authorization_code
 *          2. refresh_token
 *          3. client_credentials
 *      此过滤器的默认配置:
 *          1. accessTokenRequestConverter -> AuthenticationConverter(DelegatingAuthenticationConverter composed of OAuth2AuthorizationCodeAuthenticationConverter, OAuth2RefreshTokenAuthenticationConverter, and OAuth2ClientCredentialsAuthenticationConverter)
 *          2. authenticationProvider -> AuthenticationManager(OAuth2AuthorizationCodeAuthenticationProvider, OAuth2RefreshTokenAuthenticationProvider, and OAuth2ClientCredentialsAuthenticationProvider)
 *          3. accessTokenResponseHandler -> 处理 OAuth2AccessTokenAuthenticationToken 的内部实现并返回 OAuth2AccessTokenResponse.
 *          4. errorResponseHandler -> 处理 OAuth2AuthenticationException 的内部实现,关联到OAuth2Error并返回 OAuth2Error错误响应 ..
 *      由于我们自己需要 Resource_owner的授权授予类型
 *          所以这里自填了{@link OAuth2ResourceOwnerPasswordAuthenticationProvider} 详情查看 lightning-authorization-server
 *
 *
 *
 *  9.  OAuth2TokenIntrospectionEndpointConfigurer
 *      这个配置器提供了定制 OAuth2 Token 自省端点的配置,主要可以配置OAuth2 自省请求的相关逻辑
 *      1.AuthenticationConverter  从HttpServletRequest中抓取 OAuth2 自省请求到 OAuth2TokenIntrospectionAuthenticationToken ..
 *      2. AuthenticationProvider  认证 OAuth2TokenIntrospectionAuthenticationToken(一般是委派)
 *      3. AuthenticationSuccessHandler  处理认证过的 OAuth2TokenIntrospectionAuthenticationToken 并返回OAuth2TokenIntrospection response.
 *      4. AuthenticationFailureHandler  处理OAuth2AuthenticationException 错误并返回 OAuth2Error response.
 *
 *      同样此配置器配置了 OAuth2TokenIntrospectionEndpointFilter 注册到OAuth2 authorization server中并处理 自省请求 ..
 *      此过滤器默认配置:
 *          1. introspectionRequestConverter -> OAuth2TokenIntrospectionAuthenticationToken的抓取内部实现 ..
 *          2. authenticationProvider -> AuthenticationManager(OAuth2TokenIntrospectionAuthenticationProvider)
 *          3. introspectionResponseHandler -> 处理认证过的 OAuth2TokenIntrospectionAuthenticationToken  并返回 OAuth2TokenIntrospection 响应
 *          4. errorResponseHandler -> AuthenticationFailureHandler(内部实现,关联OAuth2Error错误并返回OAuth2Error响应)
 *
 *
 *
 * 10. OAuth2TokenRevocationEndpointConfigurer
 *      配置OAuth2 Token撤销端点,能够用来处理 OAuth2 撤销请求的逻辑配置
 *      1. AuthenticationConverter  尝试抓取从HttpServletRequest中抓取 OAuth2 revocation request  到OAuth2TokenRevocationAuthenticationToken .
 *      2. AuthenticationProvider  认证 OAuth2TokenRevocationAuthenticationToken(委派)
 *      3. AuthenticationSuccessHandler  处理认证的 OAuth2TokenRevocationAuthenticationToken  并返回OAuth2 撤销响应 ..
 *      4. AuthenticationFailureHandler  处理 OAuth2AuthenticationException 异常并返回 OAuth2Error 响应 .
 *
 *      此配置器配置了一个 OAuth2TokenRevocationEndpointFilter  ...
 *      用来处理 OAuth2 撤销请求 ..
 *      OAuth2TokenRevocationEndpointFilter 具有默认配置:
 *      1. revocationRequestConverter -> 返回 OAuth2TokenRevocationAuthenticationToken的内部实现
 *      2. authenticationProvider -> AuthenticationManager (OAuth2TokenRevocationAuthenticationProvider)
 *      3. revocationResponseHandler -> 认证的 OAuth2TokenRevocationAuthenticationToken 内部实现处理并返回 OAuth2 撤销响应 ..
 *      4. errorResponseHandler -> 关联OAuth2AuthenticationException 到OAuth2Error的内部实现并返回OAuth2Error 响应 .
 *
 *
 *
 * 11. OAuth2AuthorizationServerConfigurer
 *      配置OAuth2 授权服务器元数据端点
 *      它配置了一个 OAuth2AuthorizationServerMetadataEndpointFilter 用来处理 OAuth2 授权服务器元数据请求并返回 OAuth2AuthorizationServerMetadata response.
 *      1. jwk set 端点
 *          提供对JWK Set endpoint[https://datatracker.ietf.org/doc/html/rfc7517]的支持 ..
 *          此配置器配置了 NimbusJwkSetEndpointFilter 用来返回 JWK Set ..
 *          Jwt Set端点需要通过 JWKSource<SecurityContext> @Bean 进行注册 ..
 *
 *
 *
 * 12. OidcConfigurer
 *      进行  OpenID Connect 1.0 Provider Configuration endpoint的配置
 *      此配置器配置了一个 OidcProviderConfigurationEndpointFilter 用来返回 OidcProviderConfiguration response.
 *
 *
 *
 *
 * 13. OidcUserInfoEndpointConfigurer
 *      用来定制  OpenID Connect 1.0 UserInfo endpoint.
 *      它定义了自定义UserInfo 响应的扩展点 ..
 *      1. userInfoMapper 用来从OidcUserInfoAuthenticationContext 抓取 claims到 OidcUserinfo的实例 ..
 *      此配置器还配置了 OidcUserInfoEndpointFilter用来处理UserInfo请求 并返回 OidcUserInfo response.
 *      此过滤器的默认配置如下:
 *          1. authenticationManager -> AuthenticationManager(OidcUserInfoAuthenticationProvider) 关联了一个userInfoMapper的内部实现用来基于从在授权过程中所请求的scope的ID TOKEN中抓取
 *          标准 claims(https://openid.net/specs/openid-connect-core-1_0.html#StandardClaims)
 *          2. 通过 OAuth2TokenCustomizer<JwtEncodingContext> @Bean 定制ID Token ..
 *      此外,OpenID Connect 1.0 UserInfo endpoint 是一个OAuth2 保护的资源,也就是需要发送 bearer 访问token 来发起 Userinfo Request ..
 *      由于需要解密访问TOKEN,所以需要一个 JwtDecoder @Bean ..
 *      [https://docs.spring.io/spring-authorization-server/docs/0.3.0/reference/html/guides/how-to-userinfo.html#how-to-userinfo] 了解如何定制UserInfo 端点..
 *
 *
 *
 * 14.  OidcClientRegistrationEndpointConfigurer
 *      可以用来配置OpenID Connect 1.0 客户端注册端点(默认是禁用的),当然可以自定义配置 ..
 *      由于大多数部署不需要动态客户端注册,所以默认是禁用的 ..
 *      此配置器配置了一个OidcClientRegistrationEndpointFilter, 用来处理客户端注册请求并返回 OidcClientRegistration response.
 *      此过滤器也处理客户端读取请求并返回 OidcClientRegistration response.
 *      此过滤器的默认配置:
 *       1. authenticationManager -> AuthenticationManager(OidcClientRegistrationAuthenticationProvider)
 *       由于客户端注册端点是一个OAuth2 保护资源,所以需要bearer 访问token发起客户端注册 / 客户端读取请求 ..
 *       2. 如果发起客户端注册请求 需要访问token具有 scope client.create
 *       3. 客户端读取请求需要token 具有 scope client.read ..
 *       由于需要解密访问TOKEN,所以需要一个 JwtDecoder @Bean ..
 *
 *
 *       中央授权服务中心,相比普通的用户授权中心来说,需要额外的clientRegistrationRepository的配置 ..
 *       其他的组件可以共享,因此被抽象到了 lightning-authorization-server-configuration中 ..
 *
 *       因为希望所有的授权中心遵守oauth2的token 生成和解析规范,所以, 一般来说,只要是授权中心都会拥有
 *       oauth2 规范的一部分组件 ...
 *       例如: providerSettings / TokenSettings .. / JwkSource .. / TokenGenerator / TokenDecoder ..
 */
@AutoConfigureAfter(AuthorizationServerCommonComponentsConfiguration.class)
@Configuration
public class CentralOAuthServerComponentConfiguration {




}
