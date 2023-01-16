package com.generatera.security.authorization.server.specification.components.token;

/**
 * @author FLJ
 * @date 2023/1/16
 * @time 14:12
 * @Description lightning token 自定义器
 */
public interface LightningTokenCustomizer<T extends LightningTokenContext> {

    void customize(T tokenContext);
}
