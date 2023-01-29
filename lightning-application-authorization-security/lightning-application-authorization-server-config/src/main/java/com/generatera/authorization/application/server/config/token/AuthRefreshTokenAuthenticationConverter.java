package com.generatera.authorization.application.server.config.token;

import com.generatera.authorization.server.common.configuration.AuthorizationGrantType;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static com.generatera.authorization.application.server.config.util.AuthEndPointUtils.throwError;

/**
 * @author FLJ
 * @date 2023/1/28
 * @time 16:33
 * @Description oauth2 copy
 */
public final class AuthRefreshTokenAuthenticationConverter implements AuthenticationConverter {
    public AuthRefreshTokenAuthenticationConverter() {
    }

    @Nullable
    public Authentication convert(HttpServletRequest request) {
        String grantType = request.getParameter("grant_type");
        if (!AuthorizationGrantType.REFRESH_TOKEN.getValue().equals(grantType)) {
            return null;
        } else {
            Authentication clientPrincipal = SecurityContextHolder.getContext().getAuthentication();
            MultiValueMap<String, String> parameters = HttpRequestUtil.getParameters(request);
            String refreshToken = parameters.getFirst("refresh_token");
            if (!StringUtils.hasText(refreshToken) || parameters.get("refresh_token").size() != 1) {
                throwError("invalid_request", "refresh_token", "https://datatracker.ietf.org/doc/html/rfc6749#section-5.2");
            }

            String scope = parameters.getFirst("scope");
            if (StringUtils.hasText(scope) && parameters.get("scope").size() != 1) {
                throwError("invalid_request", "scope", "https://datatracker.ietf.org/doc/html/rfc6749#section-5.2");
            }

            // todo 不需要scope??
            Set<String> requestedScopes = null;
            if (StringUtils.hasText(scope)) {
                requestedScopes = new HashSet<>(Arrays.asList(StringUtils.delimitedListToStringArray(scope, " ")));
            }

            Map<String, Object> additionalParameters = new HashMap<>();
            parameters.forEach((key, value) -> {
                if (!key.equals("grant_type") && !key.equals("refresh_token") && !key.equals("scope")) {
                    additionalParameters.put(key, value.get(0));
                }

            });
            return new AuthRefreshTokenAuthenticationToken(refreshToken, clientPrincipal, requestedScopes, additionalParameters);
        }
    }

}