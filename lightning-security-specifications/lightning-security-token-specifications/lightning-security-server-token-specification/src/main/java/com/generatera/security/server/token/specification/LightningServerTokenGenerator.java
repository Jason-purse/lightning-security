package com.generatera.security.server.token.specification;

/**
 * @author FLJ
 * @date 2023/1/6
 * @time 12:12
 * @Description 服务器端的Token 生成器
 */
public interface LightningServerTokenGenerator<T extends LightningToken> extends LightningTokenGenerator<T,LightningAuthorizationServerSecurityContext> {

}
