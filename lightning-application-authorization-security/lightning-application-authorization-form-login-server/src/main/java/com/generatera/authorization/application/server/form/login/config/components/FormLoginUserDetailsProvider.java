package com.generatera.authorization.application.server.form.login.config.components;

import com.generatera.authorization.application.server.config.LoginGrantType;
import com.generatera.authorization.application.server.config.token.AuthRefreshTokenAuthenticationToken;
import com.generatera.authorization.application.server.config.token.LightningUserDetailsProvider;
import com.generatera.security.authorization.server.specification.LightningUserPrincipal;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.Assert;

public class FormLoginUserDetailsProvider implements LightningUserDetailsProvider {
    private final UserDetailsService userDetailsService;

    public FormLoginUserDetailsProvider(UserDetailsService userDetailsService) {
        Assert.notNull(userDetailsService,"userDetailsService must not be null !!!");
        this.userDetailsService = userDetailsService;
    }
    @Override
    public LightningUserPrincipal getUserDetails(AuthRefreshTokenAuthenticationToken authRefreshTokenAuthenticationToken, String principalName) {
        LoginGrantType loginGrantType = authRefreshTokenAuthenticationToken.getLoginGrantType();
        // 表单登录 ..
        if(loginGrantType.value().equalsIgnoreCase(LoginGrantType.FORM_LOGIN.value())) {
            return ((LightningUserPrincipal) userDetailsService.loadUserByUsername(principalName));
        }
        return null;
    }
}
