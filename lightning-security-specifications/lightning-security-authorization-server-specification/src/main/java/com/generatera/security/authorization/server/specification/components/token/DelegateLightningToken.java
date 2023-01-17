package com.generatera.security.authorization.server.specification.components.token;

import org.springframework.util.Assert;

import java.time.Instant;

/**
 * @author FLJ
 * @date 2023/1/17
 * @time 10:03
 * @Description 委派LightningToken ...
 *
 * 可以代理当前{@code LightningToken}的基本属性信息到目标{@code LightningToken} 上 ..
 * 对于仅仅只需要最终给出token 信息时,不关心其他信息,也可以用这个 ..
 *
 * 包括代理到一个Token 列表形成一个 结果 json ...
 */
public class DelegateLightningToken implements LightningToken {
    
    private final LightningToken delegate;
    
    public DelegateLightningToken(LightningToken delegate) {
        Assert.notNull(delegate," token cannot be empty !!!");
        this.delegate = delegate;
    }
   

    @Override
    public String getTokenValue() {
        return delegate.getTokenValue();
    }

    @Override
    public Instant getIssuedAt() {
        return delegate.getIssuedAt();
    }

    @Override
    public Instant getExpiresAt() {
        return delegate.getExpiresAt();
    }
}
