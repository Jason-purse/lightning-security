package com.generatera.security.authorization.server.specification;

/**
 * @author FLJ
 * @date 2023/1/3
 * @time 14:33
 * @Description 将其作为一个Bean 放入容器 ..
 */
public final  class TokenSettingsProvider {

    private final TokenSettings settings;

    public TokenSettingsProvider(TokenSettings tokenSettings) {
        this.settings = tokenSettings;
    }

    public  TokenSettings getTokenSettings() {
        return settings;
    }

}
