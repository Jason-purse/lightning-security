package com.generatera.authorization.application.server.config;

import com.generatera.authorization.server.common.configuration.AuthConfigConstant;

public interface AppAuthConfigConstant {
    public static final String APP_AUTH_SERVER_PREFIX = AuthConfigConstant.AUTH_SERVER_WITH_VERSION_PREFIX + "/app";

    public static final String APP_AUTH_SERVER_TOKEN_ROUTE_PREFIX = APP_AUTH_SERVER_PREFIX + AuthConfigConstant.TOKEN_PATTERN_PREFIX;

}
