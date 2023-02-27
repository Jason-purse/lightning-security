package com.generatera.security.authorization.server.specification;

import com.generatera.security.authorization.server.specification.components.token.JwtClaimsToUserPrincipalMapper;
import org.springframework.security.core.CredentialsContainer;
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
 * 对应表单的{@link  org.springframework.security.core.userdetails.UserDetailsService#loadUserByUsername(String)}
 * 返回的 LightningUserPrincipal 应该保证 {@code isAuthenticated()}返回结果为真 !!!
 *
 * 在应用程序的过程中可以通过扩展框架，实现用户的状态动态控制,来实现对刷新token的影响 - 特别是在jwt token的情况下 ..
 *
 *
 * @see LightningUserPrincipalConverter
 * @see JwtClaimsToUserPrincipalMapper
 *
 */
public interface LightningUserPrincipal extends UserDetails, CredentialsContainer, Serializable {


    default String getName() {
        return getUsername();
    }

    /**
     * 此标识 由框架识别.来决定是否凭证有效 ..
     *
     * 子类可以覆盖,例如可以用这个来使得刷新token 失效 ..
     */
    default  boolean isAuthenticated() {
        return isEnabled() && isAccountNonExpired() && isAccountNonLocked() && isCredentialsNonExpired();
    }

    /**
     * 子类可以选择,擦除掉凭证信息,保证账户安全 ..
     * 此方法由框架本身调用,并保持幂等 ..
     */
    @Override
    default void eraseCredentials() {
        // 默认不做任何事情 ..
    }
}
