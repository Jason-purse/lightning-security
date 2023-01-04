package com.generatera.authorization.server.common.configuration.token;

import org.springframework.security.core.AuthenticatedPrincipal;

import java.util.List;

/**
 * @author FLJ
 * @date 2023/1/3
 * @time 12:50
 * @Description Lightning UserPrincipal
 */
public interface LightningUserPrincipal extends AuthenticatedPrincipal {

    List<String> getAuthoritiesForString();
}
