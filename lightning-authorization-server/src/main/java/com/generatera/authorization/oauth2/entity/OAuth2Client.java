package com.generatera.authorization.oauth2.entity;

import com.generatera.authorization.jpa.audit.AuditDeletedDate;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
/**
 * @author FLJ
 * @date 2022/12/27
 * @time 16:44
 * @Description oauth2 客户端(应用程序代表)
 */
@Deprecated
@Entity
@Table(name = "oauth2_client")
public class OAuth2Client implements Serializable {

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
	 * @see org.springframework.security.oauth2.core.ClientAuthenticationMethod
	 */
	@Column(name = "authentication_method")
	private String clientAuthenticationMethods;

	/**
	 * 授权授予类型列表
	 * @see org.springframework.security.oauth2.core.AuthorizationGrantType
	 */
	@Column(name = "authorization_grant_type")
	private String authorizationGrantTypes;

	/**
	 * 重定向urls(仅当 授权码授予类型时才有效)
	 */
	@Column(name = "redirect_uris")
	private String redirectUris;

	/**
	 * 客户端所持有的scopes ..
	 */
	private String scopes;

	/**
	 * 是否已经注册,默认已注册 ...
	 *
	 *
	 */
	private boolean registered;

	@OneToOne(mappedBy = "oauth2Client", cascade = CascadeType.ALL, optional = false)
	private OAuth2ClientTokenSetting tokenSetting;

	@OneToOne(mappedBy = "oauth2Client", cascade = CascadeType.ALL)
	private OAuth2ClientSetting clientSetting;
	
	@Embedded
    private AuditDeletedDate audit = new AuditDeletedDate();

}
