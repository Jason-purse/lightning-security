package com.generatera.security.authorization.server.specification;

import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;

/**
 * @author FLJ
 * @date 2023/1/3
 * @time 12:50
 * @Description Lightning UserPrincipal
 *
 */
public interface LightningUserPrincipal extends UserDetails, Serializable {


    default String getName() {
        return getUsername();
    }

}
