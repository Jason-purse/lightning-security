package com.generatera.resource.server.config.token.entrypoint;

import com.generatera.resource.server.config.util.AuthHttpResponseUtil;
import com.jianyue.lightning.result.Result;
import com.jianyue.lightning.util.JsonUtil;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.Assert;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DefaultForbiddenAuthenticationEntryPoint implements LightningAuthenticationEntryPoint {

    private String errorMessage = "Not logged in !!!";

    public void setErrorMessage(String errorMessage) {
        Assert.hasText(errorMessage, "errorMessage must not be null !!!");
        this.errorMessage = errorMessage;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        AuthHttpResponseUtil.commence(response,
                JsonUtil
                        .getDefaultJsonUtil()
                        .asJSON(
                                Result.error(403, errorMessage)
                        ));
    }
}
