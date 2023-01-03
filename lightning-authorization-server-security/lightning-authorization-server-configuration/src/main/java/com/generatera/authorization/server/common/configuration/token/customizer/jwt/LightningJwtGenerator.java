package com.generatera.authorization.server.common.configuration.token.customizer.jwt;

import com.generatera.authorization.server.common.configuration.token.LightningSecurityContext;

/**
 * @author FLJ
 * @date 2023/1/3
 * @time 10:36
 * @Description Lightning Jwt Generator ..
 *
 * 需要借助jwt encoder {@link LightningJwtEncoder} 完成jwt 生成
 */
public interface LightningJwtGenerator<T extends LightningSecurityContext> {

    LightningJwt generate(T lightningSecurityContext);

}
