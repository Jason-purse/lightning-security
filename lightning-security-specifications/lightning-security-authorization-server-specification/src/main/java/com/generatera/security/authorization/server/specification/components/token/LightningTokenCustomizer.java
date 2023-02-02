package com.generatera.security.authorization.server.specification.components.token;

/**
 * @author FLJ
 * @date 2023/1/16
 * @time 14:12
 * @Description lightning token 自定义器
 *
 * 基于代理默认自动实现代理自定义 ..
 * 容器中只允许放入一个 LightningTokenCustomizer ..
 *
 * 并且当服务中存在 central-oauth2-auth-server的情况下,使用的是
 * {@link com.generatera.central.oauth2.authorization.server.configuration.components.token.LightningCentralOAuth2TokenCustomizer}
 *
 * 因为 oauth2 和 普通 token 生成方式存在很大的差异性 ...
 *
 * @see DelegateLightningTokenCustomizer
 */
public interface LightningTokenCustomizer<T extends LightningTokenContext> {

    void customize(T tokenContext);
}
