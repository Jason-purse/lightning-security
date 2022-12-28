package com.generatera.authorization.oauth2.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.Instant;

/**
 * @author FLJ
 * @date 2022/12/27
 * @time 16:50
 * @Description oauth2 客户端实体
 */
@Data
@Entity
@Table(name = "oauth2-client")
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OAuth2ClientEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_id")
    private String clientId;

    @Column(name = "client_id_issued_at")
    private Instant clientIdIssuedAt;

    @Column(name = "client_name")
    private String clientName;

    @Column(name = "client_secret")
    private String clientSecret;

    @Column(name = "client_secret_expires_at")
    private Instant clientSecretExpiresAt;

    /**
     * 客户端认证方法列表
     *
     * @see org.springframework.security.oauth2.core.ClientAuthenticationMethod
     */
    @Column(name = "authentication_method", length = 1000)
    private String clientAuthenticationMethods;

    /**
     * 授权授予类型列表
     *
     * @see org.springframework.security.oauth2.core.AuthorizationGrantType
     */
    @Column(name = "authorization_grant_type", length = 1000)
    private String authorizationGrantTypes;

    /**
     * 重定向urls(仅当 授权码授予类型时才有效)
     */
    @Column(name = "redirect_uris", length = 1000)
    private String redirectUris;

    /**
     * 客户端所持有的scopes ..
     */
    private String scopes;

    /**
     * 是否已经注册,默认没有注册(false) ...
     */
    private boolean registered;


    // token settings
    /**
     * 授权码 时常(单位 毫秒)
     */
    @Column(name = "authorization_code_time")
    private Long authorizationCodeTime;

    /**
     * access token 时常(单位毫秒)
     */
    @Column(name = "access_token_time")
    private Long accessTokenTime;

    /**
     * 刷新token 时常(单位毫秒)
     */
    @Column(name = "refresh_token_time")
    private Long refreshTokenTime;

    /**
     * 访问token 格式
     */
    @Column(name = "access_token_format")
    private String accessTokenFormat;

    /**
     * 重用刷新 token
     */
    @Column(name = "reuse_refresh_token")
    private Boolean reuseRefreshToken;

    /**
     * id token 签名算法
     *
     * @see org.springframework.security.oauth2.jose.jws.SignatureAlgorithm
     * @see org.springframework.security.oauth2.core.oidc.OidcIdToken
     */
    @Column(name = "id_token_signature_algorithm")
    private String idTokenSignatureAlgorithm;


    /**
     * token other settings
     */
    @Column(name = "token_other_settings")
    private String tokenOtherSettings;


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
    private String tokenEndpointAuthenticationSigningAlgorithm;

    /**
     * 客户端其他配置
     */
    @Column(name = "client_other_settings")
    private String clientOtherSettings;
}
