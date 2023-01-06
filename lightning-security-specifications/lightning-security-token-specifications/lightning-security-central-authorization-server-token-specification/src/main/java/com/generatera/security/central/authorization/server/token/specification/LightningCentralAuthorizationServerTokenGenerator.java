package com.generatera.security.central.authorization.server.token.specification;

import com.generatera.security.server.token.specification.LightningServerTokenGenerator;
import com.generatera.security.server.token.specification.LightningToken;

/**
 * @author FLJ
 * @date 2023/1/6
 * @time 12:14
 * @Description 应用 授权服务器的 LightningToken 生成器..
 */
public interface LightningCentralAuthorizationServerTokenGenerator<T extends LightningToken> extends LightningServerTokenGenerator<T> {
}
