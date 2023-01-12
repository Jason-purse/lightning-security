package com.generatera.security.authorization.server.specification;

import com.generatera.security.authorization.server.specification.components.token.format.jwt.LightningJwt;
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
 * @see LightningUserContext
 */
public interface JwtClaimsToUserPrincipalMapper {

    LightningUserPrincipal convert(LightningJwt jwt);
}
