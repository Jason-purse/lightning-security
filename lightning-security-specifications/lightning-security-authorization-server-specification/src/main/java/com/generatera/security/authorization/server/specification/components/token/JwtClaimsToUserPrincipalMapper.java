package com.generatera.security.authorization.server.specification.components.token;

import com.generatera.security.authorization.server.specification.LightningUserContext;
import com.generatera.security.authorization.server.specification.LightningUserPrincipal;

import java.util.Map;

/**
 * @author FLJ
 * @date 2023/1/12
 * @time 13:10
 * @Description 将 JWT 转换为 LightningUserPrincipal
 *
 * 在解析token的时候,可以通过它进行 jwt 转换为你系统中的LightningUserPrincipal .
 *
 * 从而可以通过LightningUserContext 拿取当前的用户信息
 *
 * 但是你不应该在 此映射器中做任何和 {@code LightningUserContext} 耦合的事情 ..
 * 你仅仅返回你自己的 转换逻辑即可 ...
 *
 *
 * @see LightningUserContext
 */
public interface JwtClaimsToUserPrincipalMapper {

    LightningUserPrincipal convert(Map<String,Object> claims);
}
