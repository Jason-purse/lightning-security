package com.generatera.security.authorization.server.specification;

import com.generatera.security.authorization.server.specification.components.token.JwtClaimsToUserPrincipalMapper;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;

/**
 * @author FLJ
 * @date 2023/1/3
 * @time 12:50
 * @Description Lightning UserPrincipal
 *
 * 任何应用级的用户信息,应该继承于它,并且通过在授权服务器端通过{@link LightningUserPrincipalConverter} 进行实际的  {@code LightningUserPrincipal}
 * 的序列化以及反序列化 ...
 *
 * 在资源服务器端,通过{@link JwtClaimsToUserPrincipalMapper} 转换jwt claims 信息到实际的 {@code LightningUserPrincipal},也就是这两者
 * 所使用的{@code LightningUserPrincipal} 是相同的 ...
 *
 *
 * @see LightningUserPrincipalConverter
 * @see JwtClaimsToUserPrincipalMapper
 *
 */
public interface LightningUserPrincipal extends UserDetails, Serializable {


    default String getName() {
        return getUsername();
    }

}
