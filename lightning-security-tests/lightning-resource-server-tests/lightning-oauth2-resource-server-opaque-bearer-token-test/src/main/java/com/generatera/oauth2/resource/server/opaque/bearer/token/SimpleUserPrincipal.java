package com.generatera.oauth2.resource.server.opaque.bearer.token;

import com.generatera.security.authorization.server.specification.LightningUserPrincipal;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class SimpleUserPrincipal extends User implements LightningUserPrincipal {

    public SimpleUserPrincipal(String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
    }

    public SimpleUserPrincipal(UserDetails user) {
        super(user.getUsername(),user.getPassword(),user.getAuthorities());
    }
}
