package com.generatera.security.authorization.server.specification;

import com.generatera.security.authorization.server.specification.components.provider.ProviderSettings;
import org.springframework.util.Assert;

/**
 * @author FLJ
 * @date 2023/1/4
 * @time 10:08
 * @Description 作为授权服务器的 基本提供商信息配置 ...
 * 例如你可以扩展去提供自己的 一些额外的信息,例如公司名称,电话,email,包括其他信息 ..
 *
 * 目前固有的一些基础信息是 照搬spring -oauth2的 token相关的规范 ..
 * 1. token 省查端点
 * 2. token 撤销端点
 * 3. issuer url (本质上它,也是可以让资源服务器进一步获取 jwk url 进行进一步配置的可选条件) ..
 * 4. jwk url
 */
public class ProviderSettingsProvider {

    private final ProviderSettings providerSettings;

    public ProviderSettingsProvider(ProviderSettings providerSettings) {
        Assert.notNull(providerSettings,"providerSettings must not be null !!!");
        this.providerSettings = providerSettings;
    }

    public ProviderSettings getProviderSettings() {
        return providerSettings;
    }
}
