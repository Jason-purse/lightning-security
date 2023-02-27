package com.generatera.authorization.application.server.form.login.config.components;

import com.generatera.security.authorization.server.specification.LightningUserPrincipal;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * 此方法负责用户信息抓取,并在认证成功之后,实现已经认证用户的信息交换 !!!
 */
public interface LightningUserDetailService extends UserDetailsService {

    /**
     * 返回的{@code LightningUserPrincipal} 是已经认证的 !!!
     */
    default LightningUserPrincipal mapAuthenticatedUser(LightningUserPrincipal userPrincipal) {
        return userPrincipal;
    }

}
