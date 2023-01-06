package com.generatera.resource.server.specification.token.jwt.bearer;


import com.generatera.resource.server.config.token.LightningAuthError;
import com.generatera.resource.server.config.token.entrypoint.LightningAuthenticationEntryPoint;
import com.generatera.resource.server.config.token.LightningAuthenticationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author FLJ
 * @date 2023/1/5
 * @time 13:37
 * @Description BearerToken Authentication EntryPoint ...
 *
 * BearerToken AuthenticationEntry Point 主要是进行 Token 的获取工作 ..
 * 向客户端发送 WWW-Authenticate 请求头 ..
 *
 * 当使用OAuth2-bearer token的时候,此端点不会使用 ... 或者扩展代理到OAuth-bearer token对应的认证端点上  ..
 */
public class BearerTokenAuthenticationEntryPoint implements LightningAuthenticationEntryPoint {

    private String realmName;

    public BearerTokenAuthenticationEntryPoint() {
    }

    @Override
    public final void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        Map<String, String> parameters = new LinkedHashMap<>();
        if (this.realmName != null) {
            parameters.put("realm", this.realmName);
        }

        if (exception instanceof LightningAuthenticationException) {
            LightningAuthError error = ((LightningAuthenticationException)exception).getError();
            parameters.put("error", error.getErrorCode());
            if (StringUtils.hasText(error.getDescription())) {
                parameters.put("error_description", error.getDescription());
            }

            if (StringUtils.hasText(error.getUri())) {
                parameters.put("error_uri", error.getUri());
            }
        }

        internalExceptionHandle(exception);

        String wwwAuthenticate = computeWWWAuthenticateHeaderValue(parameters);
        response.addHeader("WWW-Authenticate", wwwAuthenticate);
        response.setStatus(status.value());
    }

    protected void internalExceptionHandle(AuthenticationException exception) {
        // can ext
    }

    public void setRealmName(String realmName) {
        this.realmName = realmName;
    }

    private static String computeWWWAuthenticateHeaderValue(Map<String, String> parameters) {
        StringBuilder wwwAuthenticate = new StringBuilder();
        wwwAuthenticate.append("Bearer");
        if (!parameters.isEmpty()) {
            wwwAuthenticate.append(" ");
            int i = 0;

            for(Iterator<Map.Entry<String,String>> var3 = parameters.entrySet().iterator(); var3.hasNext(); ++i) {
                Map.Entry<String, String> entry = var3.next();
                wwwAuthenticate.append(entry.getKey()).append("=\"").append(entry.getValue()).append("\"");
                if (i != parameters.size() - 1) {
                    wwwAuthenticate.append(", ");
                }
            }
        }

        return wwwAuthenticate.toString();
    }
}
