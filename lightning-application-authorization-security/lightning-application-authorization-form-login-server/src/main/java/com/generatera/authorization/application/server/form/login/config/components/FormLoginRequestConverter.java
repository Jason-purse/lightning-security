package com.generatera.authorization.application.server.form.login.config.components;

import com.generatera.authorization.application.server.config.LoginGrantType;
import com.generatera.authorization.application.server.config.token.AuthParameterNames;
import com.generatera.authorization.application.server.config.token.HttpRequestUtil;
import com.generatera.authorization.application.server.config.util.AuthEndPointUtils;
import com.generatera.authorization.server.common.configuration.LightningAuthorizationGrantType;
import com.generatera.authorization.server.common.configuration.authorization.LightningAuthenticationConverter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @author FLJ
 * @date 2023/1/29
 * @time 14:53
 * @Description 表单登录  请求转换器
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class FormLoginRequestConverter implements LightningAuthenticationConverter {

    private String usernameParameter = AuthParameterNames.USERNAME;

    private String passwordParameter = AuthParameterNames.PASSWORD;

    public void setUsernameParameter(String usernameParameter) {

        Assert.hasText(usernameParameter, "username parameter must not be null !!!");
        this.usernameParameter = usernameParameter;
    }

    public void setPasswordParameter(String passwordParameter) {
        Assert.hasText(passwordParameter, "password parameter must not be null !!!");
        this.passwordParameter = passwordParameter;
    }

    @Override
    public Authentication convert(HttpServletRequest request, HttpServletResponse response) {


        MultiValueMap<String, String> parameters = HttpRequestUtil.getParameters(request);
        String loginRequestType = parameters.getFirst(AuthParameterNames.LOGIN_GRANT_TYPE);

        String grant_type = parameters.getFirst("grant_type");
        if (!LightningAuthorizationGrantType.ACCESS_TOKEN.getValue().equalsIgnoreCase(grant_type)) {
            return null;
        }

        // 处理表单登录 ..
        if (StringUtils.hasText(loginRequestType) &&  !loginRequestType.equalsIgnoreCase(LoginGrantType.FORM_LOGIN.value())) {
            return null;
        }

        if (!request.getMethod().equals("POST")) {
            AuthEndPointUtils.throwError("invalid_request", "authorization_request_method, Only POST request is supported!!!", "");
        }
        String username = parameters.getFirst(usernameParameter);
        String password = parameters.getFirst(passwordParameter);

        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            AuthEndPointUtils.throwError("invalid_request", "authorization_request_param username or password must not be null !!!", "");
        }
        Map<String, Object> additionalParameters = new HashMap<>();
        parameters.forEach((key, value) -> {
            if (!key.equals("grant_type") && !key.equals("refresh_token") && !key.equals("scope")) {
                additionalParameters.put(key, value.get(0));
            }

        });
        // 转换为 username / password 认证 token
        return new UsernamePasswordAuthenticationToken(username, password);
    }
}
