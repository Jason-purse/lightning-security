package com.generatera.security.application.authorization.server.token.specification;

/**
 * @author FLJ
 * @date 2023/1/3
 * @time 11:30
 * @Description  生成 Lightning Authentication Token
 *
 */
public interface LightningApplicationLevelAuthenticationTokenGenerator {

    LightningApplicationLevelAuthenticationToken  generate(LightningApplicationLevelAuthenticationSecurityContext securityContext);
}
