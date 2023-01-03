package com.generatera.authorization.server.common.configuration.token;
/**
 * @author FLJ
 * @date 2023/1/3
 * @time 11:30
 * @Description  生成 Lightning Authentication Token
 *
 */
public interface LightningAuthenticationTokenGenerator {

    public LightningAuthenticationToken generate(LightningAuthenticationSecurityContext securityContext);
}
