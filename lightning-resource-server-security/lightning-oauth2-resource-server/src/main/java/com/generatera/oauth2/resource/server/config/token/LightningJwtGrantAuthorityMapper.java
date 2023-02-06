package com.generatera.oauth2.resource.server.config.token;

import com.generatera.security.authorization.server.specification.components.token.format.jwt.LightningJwt;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
/**
 * @author FLJ
 * @date 2023/2/6
 * @time 17:19
 * @Description 用户感知Lightning Jwt 而无需感知 {@link org.springframework.security.oauth2.jwt.Jwt}
 */
public interface LightningJwtGrantAuthorityMapper extends Converter<LightningJwt, Collection<GrantedAuthority>> {


}


