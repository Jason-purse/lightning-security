package com.generatera.security.authorization.server.specification.components.token;

/**
 * @author FLJ
 * @date 2023/1/16
 * @time 15:20
 * @Description 刷新Token上下文 ...
 *
 * // TODO: 2023/1/17  是否需要刷新token上下文
 */
public class LightningRefreshTokenContext extends DefaultLightningTokenContext {
    public LightningRefreshTokenContext(DefaultLightningTokenContext tokenContext) {
        super(tokenContext.getContexts());
    }
}
