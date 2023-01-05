package com.generatera.resource.server.config.token;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
/**
 * @author FLJ
 * @date 2023/1/5
 * @time 13:54
 * @Description Lightning AuthenticationToken , 隔绝 spring-security的依赖
 */
public abstract class LightningAuthenticationToken extends AbstractAuthenticationToken {
    public LightningAuthenticationToken(Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
    }
}
