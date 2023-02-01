package com.generatera.security.authorization.server.specification;

import com.generatera.security.authorization.server.specification.components.provider.ProviderSettings;

/**
 * @author FLJ
 * @date 2023/2/1
 * @time 10:04
 * @Description Provider mark 接口
 */
public interface AuthServerProvider {

    ProviderSettings getProviderSettings();
}
