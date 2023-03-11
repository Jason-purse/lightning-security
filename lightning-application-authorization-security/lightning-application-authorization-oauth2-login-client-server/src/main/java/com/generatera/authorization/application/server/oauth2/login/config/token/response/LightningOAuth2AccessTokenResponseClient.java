package com.generatera.authorization.application.server.oauth2.login.config.token.response;

import org.springframework.security.oauth2.client.endpoint.AbstractOAuth2AuthorizationGrantRequest;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;

/**
 * 扩展为 Lightning  access Token Response Client
 *
 * 主要是负责根据OAuth2 授权请求中访问 token 端点获取 access token ...
 *
 * 授权授予请求有非常多 ..(但是目前系统中password 授予类型 oauth2 授权服务器没有支持,需要做额外扩展,
 * 详情查看 oauth2-xxx-password-grant-support-server... 模块)
 * 所以,如果需要保存授权请求以作验证之用,可能需要做额外的扩展,但是应该也不需要 ..
 * {@link com.generatera.authorization.application.server.oauth2.login.config.authorization.request.LightningOAuth2AuthorizationRequestResolver}
 * {@link com.generatera.authorization.application.server.oauth2.login.config.authorization.request.LightningAuthorizationRequestRepository}
 */
public interface LightningOAuth2AccessTokenResponseClient<T extends AbstractOAuth2AuthorizationGrantRequest> extends OAuth2AccessTokenResponseClient<T> {
}
