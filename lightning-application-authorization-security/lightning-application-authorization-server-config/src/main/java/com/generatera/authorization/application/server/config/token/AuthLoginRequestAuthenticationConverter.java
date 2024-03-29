package com.generatera.authorization.application.server.config.token;

import com.generatera.authorization.server.common.configuration.authorization.LightningAuthenticationConverter;
import com.generatera.security.authorization.server.specification.components.authorization.LightningAuthError;
import com.generatera.security.authorization.server.specification.components.authorization.LightningAuthenticationException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedList;
import java.util.List;

/**
 * @author FLJ
 * @date 2023/1/29
 * @time 13:56
 * @Description 授权请求认证转换器 ...
 * <p>
 * 目前内部系统支持,表单登录,oauth2 ..
 */
public final class AuthLoginRequestAuthenticationConverter implements LightningAuthenticationConverter {

    private static final String DEFAULT_ERROR_URI = "https://datatracker.ietf.org/doc/html/rfc6749#section-4.1.2.1";
    private static final String PKCE_ERROR_URI = "https://datatracker.ietf.org/doc/html/rfc7636#section-4.4.1";
    private static final Authentication ANONYMOUS_AUTHENTICATION = new AnonymousAuthenticationToken("anonymous", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));

    private List<LightningAuthenticationConverter> authenticationConverters = new LinkedList<>();

    public AuthLoginRequestAuthenticationConverter(LightningAuthenticationConverter... converters) {
        this(List.of(converters));
    }

    public AuthLoginRequestAuthenticationConverter(List<LightningAuthenticationConverter> converters) {
        this.authenticationConverters.addAll(converters);
    }


    public void addAuthenticationConverters(LightningAuthenticationConverter... converters) {
        authenticationConverters.addAll(List.of(converters));
    }

    public void setAuthenticationConverters(LightningAuthenticationConverter... converters) {
        this.authenticationConverters = List.of(converters);
    }


    public Authentication convert(HttpServletRequest request, HttpServletResponse response) {
        if (authenticationConverters == null || authenticationConverters.isEmpty()) {
            throwError("invalid_request", "authentication converters","no authenticationConverter can resolve login request !!!");
        }

        for (LightningAuthenticationConverter authenticationConverter : authenticationConverters) {
            Authentication convert = authenticationConverter.convert(request,response);
            if (convert != null) {
                return convert;
            }
        }
        return null;
    }


    private static void throwError(String errorCode, String parameterName) {
        throwError(errorCode, parameterName, "https://datatracker.ietf.org/doc/html/rfc6749#section-4.1.2.1");
    }

    private static void throwError(String errorCode, String parameterName, String errorUri) {
        LightningAuthError error = new LightningAuthError(errorCode, "Auth Parameter: " + parameterName, errorUri);
        throw new LightningAuthenticationException(error, (Throwable) null);
    }
}