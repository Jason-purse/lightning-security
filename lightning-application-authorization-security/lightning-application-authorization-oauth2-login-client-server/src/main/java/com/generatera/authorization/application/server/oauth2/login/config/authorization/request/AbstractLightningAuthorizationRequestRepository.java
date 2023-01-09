package com.generatera.authorization.application.server.oauth2.login.config.authorization.request;

import com.generatera.authorization.application.server.oauth2.login.config.model.entity.AuthorizationRequestEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
/**
 * @author FLJ
 * @date 2023/1/9
 * @time 10:11
 * @Description 记录授权请求 ... 用作授权结果对比 ..
 */
public abstract class AbstractLightningAuthorizationRequestRepository implements LightningAuthorizationRequestRepository {

    private final Converter<AuthorizationRequestEntity, OAuth2AuthorizationRequest> converter =
            new AuthorizationRequestConverter();

    private final Converter<OAuth2AuthorizationRequest, AuthorizationRequestEntity> infoConverter =
            new AuthorizationRequestEntityConverter();

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        Assert.notNull(request, "request cannot be null");
        String stateParameter = this.getStateParameter(request);
        if (stateParameter == null) {
            return null;
        } else {
            return this.getAuthorizationRequest(stateParameter);
        }
    }

    private String getStateParameter(HttpServletRequest request) {
        return request.getParameter("state");
    }

    private OAuth2AuthorizationRequest getAuthorizationRequest(String stateParameter) {
        AuthorizationRequestEntity auth2AuthorizationRequest = getInternalAuthorizationRequestEntity(stateParameter);
        if (auth2AuthorizationRequest != null) {
            return converter.convert(auth2AuthorizationRequest);
        }
        return null;
    }

    protected abstract AuthorizationRequestEntity getInternalAuthorizationRequestEntity(String stateParameter);

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request, HttpServletResponse response) {
        saveAuthorizationRequestEntity(infoConverter.convert(authorizationRequest));
    }

    protected abstract void saveAuthorizationRequestEntity(AuthorizationRequestEntity entity);


    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request) {
        String stateParameter = getStateParameter(request);
        if(StringUtils.hasText(stateParameter)) {
            AuthorizationRequestEntity authorizationRequestEntity = removeAuthorizationRequestEntity(stateParameter);
            if(authorizationRequestEntity != null) {
                return converter.convert(authorizationRequestEntity);
            }
        }
        return null;
    }


    protected abstract AuthorizationRequestEntity removeAuthorizationRequestEntity(String stateParameter);
}
