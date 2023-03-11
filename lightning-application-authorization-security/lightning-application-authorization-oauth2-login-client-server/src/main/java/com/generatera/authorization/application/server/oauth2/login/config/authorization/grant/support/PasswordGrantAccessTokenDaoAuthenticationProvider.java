package com.generatera.authorization.application.server.oauth2.login.config.authorization.grant.support;

import com.generatera.authorization.application.server.config.token.AuthAccessTokenAuthenticationToken;
import com.generatera.authorization.application.server.config.util.AuthEndPointUtils;
import com.generatera.authorization.application.server.oauth2.login.config.authority.LightningOAuth2GrantedAuthoritiesMapper;
import com.generatera.authorization.application.server.oauth2.login.config.client.authorized.LightningOAuth2AuthorizedClientRepository;
import com.generatera.authorization.application.server.oauth2.login.config.token.OAuth2LoginExtAuthenticationToken;
import com.generatera.authorization.application.server.oauth2.login.config.token.PasswordGrantAuthorizationRequestAuthentication;
import com.generatera.authorization.application.server.oauth2.login.config.token.response.LightningOAuth2AccessTokenResponseClient;
import com.jianyue.lightning.boot.starter.util.OptionalFlux;
import com.jianyue.lightning.boot.starter.util.dataflow.Context;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.endpoint.OAuth2PasswordGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.util.Assert;

import java.util.Collection;

/**
 * 支持 password grant 授权登陆 ...
 * 让用户 提交用户 / 密码 .. 与中央授权服务器鉴权 ...
 *
 * 然后实现相同的动作, 进行用户信息合成 ...
 */
public abstract class PasswordGrantAccessTokenDaoAuthenticationProvider implements LightningOAuth2PasswordGrantDaoAuthenticationProvider {

    private final LightningOAuth2AccessTokenResponseClient<OAuth2PasswordGrantRequest> auth2AccessTokenResponseClient;

    // 认证结果转换器 ..
    // 将一个OAuth2LoginExtAuthenticationToken 转换为 OAuth2AuthenticationToken ..
    private Converter<OAuth2LoginExtAuthenticationToken, OAuth2AuthenticationToken> authenticationResultConverter = this::createAuthenticationResult;

    private LightningOAuth2GrantedAuthoritiesMapper authoritiesMapper = (authorities) -> {
        return authorities;
    };

    /**
     * 已经授权的客户端仓库 ..
     */
    private final LightningOAuth2AuthorizedClientRepository authorizedClientRepository;


    private final LightningOAuth2UserLoader userService;

    public PasswordGrantAccessTokenDaoAuthenticationProvider(
            LightningOAuth2AccessTokenResponseClient<OAuth2PasswordGrantRequest> auth2AccessTokenResponseClient,
            LightningOAuth2UserLoader userService,
            LightningOAuth2AuthorizedClientRepository auth2AuthorizedClientRepository) {
        Assert.notNull(auth2AccessTokenResponseClient,"access response client must not be null");
        Assert.notNull(userService,"user service must not be null");
        Assert.notNull(auth2AuthorizedClientRepository,"auth2AuthorizedClientRepository must not be null");
        this.auth2AccessTokenResponseClient = auth2AccessTokenResponseClient;
        this.userService = userService;
        this.authorizedClientRepository = auth2AuthorizedClientRepository;
    }

    public void setAuthenticationResultConverter(Converter<OAuth2LoginExtAuthenticationToken, OAuth2AuthenticationToken> authenticationResultConverter) {
        Assert.notNull(authenticationResultConverter,"authenticationResultConverter must not be null");
        this.authenticationResultConverter = authenticationResultConverter;
    }

    @Override
    public Authentication authenticate(AuthAccessTokenAuthenticationToken authentication) {
        if (authentication.getAuthentication() instanceof PasswordGrantAuthorizationRequestAuthentication authorizationRequestAuthentication) {
            OAuth2AccessTokenResponse accessTokenResponse = this.getResponse(authorizationRequestAuthentication);
            ClientRegistration clientRegistration = authorizationRequestAuthentication.getClientRegistration();

            additionalAssert(authorizationRequestAuthentication,accessTokenResponse);
            OAuth2User user = this.userService.load(authorizationRequestAuthentication, accessTokenResponse);
            Collection<? extends GrantedAuthority> mappedAuthorities = this.authoritiesMapper.mapAuthorities(user.getAuthorities());
            // 不需要创建
            UsernamePasswordAuthenticationToken authenticationResult = UsernamePasswordAuthenticationToken.authenticated(user, null, mappedAuthorities);
            authenticationResult.setDetails(authentication.getDetails());

            // 认证成功,创建一个授权完成的client ...(它主要包含三大内容, 第一个registration , 对应的principalName 映射到对应的访问 token,  以及刷新token ..
            OAuth2AuthorizedClient authorizedClient = new OAuth2AuthorizedClient(
                    authorizationRequestAuthentication.getClientRegistration(),
                    authenticationResult.getName(),
                    accessTokenResponse.getAccessToken(), accessTokenResponse.getRefreshToken());

            OAuth2AuthenticationToken token = this.authenticationResultConverter.convert(
                    new OAuth2LoginExtAuthenticationToken(
                            clientRegistration,
                            user,
                            mappedAuthorities,
                            accessTokenResponse.getAccessToken(),
                            accessTokenResponse.getRefreshToken()
                    )
            );

            // 然后保存到 授权完成的客户端仓库 ...
            this.authorizedClientRepository.saveAuthorizedClient(authorizedClient, token, authorizationRequestAuthentication.getRequest(), authorizationRequestAuthentication.getResponse());

            return authenticationResult;

        }
        return null;
    }


    OAuth2AccessTokenResponse getResponse(PasswordGrantAuthorizationRequestAuthentication requestAuthentication) {
        try {
            return Context
                    .<Void, OAuth2AccessTokenResponse>of()
                    .addDataFlowHandler(ctx -> OptionalFlux.of(requestAuthentication.getAdditionalParameters())
                            .map(ele -> {
                                return new OAuth2PasswordGrantRequest(
                                        requestAuthentication.getClientRegistration(),
                                        requestAuthentication.getUsername(),
                                        requestAuthentication.getPassword());
                            })
                            .map(auth2AccessTokenResponseClient::getTokenResponse)
                            .consume(ctx::setResult)
                    )
                    .start()
                    .getResult();
        }catch (Exception e) {
            if(e.getCause() != null) {
                // 处理 OAuth2AuthenticationException
                if(e.getCause() instanceof OAuth2AuthorizationException exception) {
                    OAuth2Error error = exception.getError();
                    AuthEndPointUtils.throwError(error.getErrorCode(),error.getDescription(),error.getDescription());
                    return null;
                }
                AuthEndPointUtils.throwError("invalid request",e.getCause().getMessage(),"");
                return null;
            }
            // 进行异常捕获 ..
            AuthEndPointUtils.throwError("invalid request",e.getMessage(),"");
            return null;
        }
    }


    public final void setAuthoritiesMapper(LightningOAuth2GrantedAuthoritiesMapper authoritiesMapper) {
        Assert.notNull(authoritiesMapper, "authoritiesMapper cannot be null");
        this.authoritiesMapper = authoritiesMapper;
    }


    protected void additionalAssert(PasswordGrantAuthorizationRequestAuthentication authentication,OAuth2AccessTokenResponse accessTokenResponse) {

    }

    private OAuth2AuthenticationToken createAuthenticationResult(OAuth2LoginExtAuthenticationToken authenticationToken) {
        // 直接将对应的身份, 授权集合,registrationId 获取构建返回即可 ..
        return new OAuth2AuthenticationToken(authenticationToken.getPrincipal(), authenticationToken.getAuthorities(),
                authenticationToken.getClientRegistration().getRegistrationId());
    }

}
