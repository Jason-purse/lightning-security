package com.generatera.authorization.application.server.oauth2.login.config.authorization.request;

import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

/**
 * 授权请求仓库
 *
 * 目前只支持 授权码,但是应该也支持其他授权授予类型 ...
 */
public interface LightningAuthorizationRequestRepository extends AuthorizationRequestRepository<OAuth2AuthorizationRequest> {


}
