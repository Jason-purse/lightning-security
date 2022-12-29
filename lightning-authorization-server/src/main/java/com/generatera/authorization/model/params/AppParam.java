package com.generatera.authorization.model.params;

import com.jianyue.lightning.boot.starter.generic.crud.service.entity.IdSupport;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2TokenFormat;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;

import javax.persistence.Column;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppParam implements ServerParam, IdSupport<Long> {


    private Long id;


    /**
     * 客户端id
     */
    private String clientId;

    /**
     * 霎那
     */
    private Instant clientIdIssuedAt;


    private String clientName;


    private String clientSecret;


    private Instant clientSecretExpiresAt;

    /**
     * 客户端认证方法列表
     *
     * @see org.springframework.security.oauth2.core.ClientAuthenticationMethod
     */
    private List<ClientAuthenticationMethod> clientAuthenticationMethods;

    /**
     * 授权授予类型列表
     *
     * @see org.springframework.security.oauth2.core.AuthorizationGrantType
     */
    private List<AuthorizationGrantType> authorizationGrantTypes;

    /**
     * 重定向urls(仅当 授权码授予类型时才有效)
     */
    private List<String> redirectUris;

    /**
     * 客户端所持有的scopes ..
     */
    private List<String> scopes;

    /**
     * 是否已经注册,默认没有注册(false) ...
     */
    private Boolean registered;


    // token settings
    /**
     * 授权码 时常(单位 毫秒)
     */
    private Long authorizationCodeTime;

    /**
     * access token 时常(单位毫秒)
     */
    private Long accessTokenTime;

    /**
     * 刷新token 时常(单位毫秒)
     */
    private Long refreshTokenTime;

    /**
     * 访问token 格式
     */
    private OAuth2TokenFormat accessTokenFormat;

    /**
     * 重用刷新 token
     */
    private Boolean reuseRefreshToken;

    /**
     * id token 签名算法
     *
     * @see org.springframework.security.oauth2.jose.jws.SignatureAlgorithm
     * @see org.springframework.security.oauth2.core.oidc.OidcIdToken
     */
    private SignatureAlgorithm idTokenSignatureAlgorithm;


    /**
     * token other settings
     */
    @Column(name = "token_other_settings")
    private Map<String,Object> tokenOtherSettings;


    // client settings
    /**
     * 是否需要 proof key
     */
    @Column(name = "require_proof_key")
    private Boolean requireProofKey;

    /**
     * 是否需要 协商页面
     */
    @Column(name = "require_authorization_consent")
    private Boolean requireAuthorizationConsent;

    /**
     * 标识客户端的 json web key set的 url
     */
    @Column(name = "jwk_set_url")
    private String jwkSetUrl;

    /**
     * token 端点签名算法
     */
    @Column(name = "token_endpoint_authentication_signing_algorithm")
    private SignatureAlgorithm tokenEndpointAuthenticationSigningAlgorithm;

    /**
     * 客户端其他配置
     */
    @Column(name = "client_other_settings")
    private Map<String,Object> clientOtherSettings;

}
