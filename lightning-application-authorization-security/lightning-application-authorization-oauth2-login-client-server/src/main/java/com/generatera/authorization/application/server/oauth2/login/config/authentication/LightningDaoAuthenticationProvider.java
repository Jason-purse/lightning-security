package com.generatera.authorization.application.server.oauth2.login.config.authentication;

import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * @author FLJ
 * @date 2023/1/28
 * @time 17:07
 * @Description 增强,提供token 生成功能
 */
public class LightningDaoAuthenticationProvider extends DaoAuthenticationProvider {

    private

    @Override
    protected Authentication createSuccessAuthentication(Object principal, Authentication authentication, UserDetails user) {
        return super.createSuccessAuthentication(principal, authentication, user);
    }
}
