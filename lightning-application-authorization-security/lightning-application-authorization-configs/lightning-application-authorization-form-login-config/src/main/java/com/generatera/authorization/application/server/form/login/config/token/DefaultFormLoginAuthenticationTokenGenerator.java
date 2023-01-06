package com.generatera.authorization.application.server.form.login.config.token;

import com.generatera.security.application.authorization.server.token.specification.LightningApplicationLevelAuthenticationSecurityContext;
import com.generatera.security.application.authorization.server.token.specification.LightningApplicationLevelAuthenticationToken;
import com.generatera.security.application.authorization.server.token.specification.LightningApplicationLevelAuthenticationTokenGenerator;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.util.Assert;

/**
 * @author FLJ
 * @date 2023/1/3
 * @time 9:40
 * @Description 表单登录情况下的 token 生成器 ..
 * <p>
 * 这种情况下,我们有两种选择 ...
 * 1. 简单生成 自省token
 * 2. 将用户信息放入token ..
 */
public class DefaultFormLoginAuthenticationTokenGenerator implements FormLoginAuthenticationTokenGenerator {

    private FormLoginAuthenticationTokenGenerator tokenGenerator;

    public DefaultFormLoginAuthenticationTokenGenerator(JWKSource<SecurityContext> jwkSource) {
        this.tokenGenerator = new FormLoginAuthenticationTokenGenerator() {
            private final LightningApplicationLevelAuthenticationTokenGenerator tokenGenerator = new DefaultFormLoginAuthenticationTokenGenerator(jwkSource);

            @Override
            public LightningApplicationLevelAuthenticationToken generate(LightningApplicationLevelAuthenticationSecurityContext securityContext) {
                return tokenGenerator.generate(securityContext);
            }
        };
    }


    public void setTokenGenerator(FormLoginAuthenticationTokenGenerator tokenGenerator) {
        Assert.notNull(tokenGenerator, "tokenGenerator must not be null !!!");
        this.tokenGenerator = tokenGenerator;
    }


    @Override
    public LightningApplicationLevelAuthenticationToken generate(LightningApplicationLevelAuthenticationSecurityContext securityContext) {
        return tokenGenerator.generate(securityContext);
    }
}
