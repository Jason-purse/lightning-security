package com.generatera.authorization.application.server.config.token;

import com.generatera.authorization.application.server.config.LoginGrantType;
import com.generatera.authorization.server.common.configuration.LightningAuthorizationGrantType;
import com.generatera.authorization.server.common.configuration.authorization.LightningAuthenticationConverter;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

import static com.generatera.authorization.application.server.config.util.AuthEndPointUtils.throwError;

/**
 * @author FLJ
 * @date 2023/1/28
 * @time 16:33
 * @Description oauth2 copy
 */
public class AuthRefreshTokenAuthenticationConverter implements LightningAuthenticationConverter {
    public AuthRefreshTokenAuthenticationConverter() {
    }

    @Nullable
    public Authentication convert(HttpServletRequest request, HttpServletResponse response) {
        String grantType = request.getParameter("grant_type");
        if (!LightningAuthorizationGrantType.REFRESH_TOKEN.getValue().equals(grantType)) {
            return null;
        } else {

            String login_grant_type = request.getParameter("login_grant_type");
            if(!StringUtils.hasText(login_grant_type)) {
                // 伪装为 表单登录 ...
                login_grant_type = LoginGrantType.FORM_LOGIN.value();
            }

            MultiValueMap<String, String> parameters = HttpRequestUtil.getParameters(request);
            String refreshToken = parameters.getFirst("refresh_token");
            if (!StringUtils.hasText(refreshToken) || parameters.get("refresh_token").size() != 1) {
                throwError("invalid_request", "refresh_token", "https://datatracker.ietf.org/doc/html/rfc6749#section-5.2");
            }
            return doConvert(refreshToken,login_grant_type, parameters);
        }
    }

    protected Authentication doConvert(String refreshToken, String loginGrantType,MultiValueMap<String, String> parameters) {
        Map<String, Object> additionalParameters = new HashMap<>();
        parameters.forEach((key, value) -> {
            if (!key.equals("grant_type") && !key.equals("refresh_token")) {
                additionalParameters.put(key, value.get(0));
            }

        });

        return new AuthRefreshTokenAuthenticationToken(LoginGrantType.of(loginGrantType),refreshToken, additionalParameters);
    }

}