package com.generatera.authorization.application.server.form.login.config;

import com.generatera.security.authorization.server.specification.LightningUserPrincipal;
import com.jianyue.lightning.boot.starter.util.ElvisUtil;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public class PlainLightningUserPrincipal implements LightningUserPrincipal {

    private final UserDetails userDetails;

    public PlainLightningUserPrincipal(UserDetails userDetails) {
        this.userDetails = userDetails;
    }

    @Override
    public List<String> getAuthoritiesForString() {
        return ElvisUtil
                .acquireNotNullList_Empty(
                        ElvisUtil.isNotEmptyFunction(userDetails.getAuthorities(), authorities ->
                                authorities.stream().map(GrantedAuthority::getAuthority).toList()
                        )
                );
    }

    @Override
    public String getName() {
        return userDetails.getUsername();
    }
}
