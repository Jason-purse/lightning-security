package com.generatera.security.authorization.server.specification;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 默认的LightningUserDetails
 *
 * 当没有提供自己的UserDetailsService 则默认使用它 ..
 *
 * 一般来说,应用应该自己提供自己的UserDetailsService 并 给出实际的 LightningUserPrincipal
 */
public class DefaultLightningUserDetails extends User implements LightningUserPrincipal {
    public DefaultLightningUserDetails(UserDetails user) {
        super(user.getUsername(),user.getPassword(),user.isEnabled(),user.isAccountNonExpired(),user.isCredentialsNonExpired(), user.isAccountNonLocked(),user.getAuthorities());
    }
}
