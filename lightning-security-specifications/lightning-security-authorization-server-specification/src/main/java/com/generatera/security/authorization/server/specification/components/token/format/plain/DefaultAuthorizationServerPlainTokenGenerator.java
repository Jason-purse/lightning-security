package com.generatera.security.authorization.server.specification.components.token.format.plain;


import com.generatera.security.authorization.server.specification.components.token.LightningToken;
import com.generatera.security.authorization.server.specification.components.token.LightningTokenContext;
import com.generatera.security.authorization.server.specification.components.token.LightningTokenGenerator;

/**
 * @author FLJ
 * @date 2023/1/6
 * @time 13:03
 * @Description 直接通过 UUID 生成一个简单Token
 */
public class DefaultAuthorizationServerPlainTokenGenerator implements LightningTokenGenerator<LightningToken> {

    @Override
    public LightningToken generate(LightningTokenContext context) {
        String tokenValue = UuidUtil.nextId();
        return new DefaultPlainToken(tokenValue);
    }
}
