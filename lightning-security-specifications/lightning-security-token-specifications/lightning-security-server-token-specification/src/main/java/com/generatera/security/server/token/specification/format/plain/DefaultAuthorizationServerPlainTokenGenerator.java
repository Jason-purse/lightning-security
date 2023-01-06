package com.generatera.security.server.token.specification.format.plain;


import com.generatera.security.server.token.specification.LightningAuthorizationServerSecurityContext;
import com.generatera.security.server.token.specification.LightningToken;

/**
 * @author FLJ
 * @date 2023/1/6
 * @time 13:03
 * @Description 直接通过 UUID 生成一个简单Token
 */
public class DefaultAuthorizationServerPlainTokenGenerator implements LightningAuthorizationServerPlainTokenGenerator {

    @Override
    public LightningToken.PlainToken generate(LightningAuthorizationServerSecurityContext context) {
        String tokenValue = UuidUtil.nextId();
        return new DefaultPlainToken(tokenValue);
    }
}
