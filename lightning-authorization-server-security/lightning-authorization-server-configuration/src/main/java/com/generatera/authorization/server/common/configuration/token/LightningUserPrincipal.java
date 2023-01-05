package com.generatera.authorization.server.common.configuration.token;

import org.springframework.security.core.AuthenticatedPrincipal;

import java.io.Serializable;
import java.util.List;

/**
 * @author FLJ
 * @date 2023/1/3
 * @time 12:50
 * @Description Lightning UserPrincipal
 *
 * 必须可序列化
 */
public interface LightningUserPrincipal extends AuthenticatedPrincipal, Serializable {

    List<String> getAuthoritiesForString();

}
