package com.generatera.authorization.server.common.configuration;

/**
 * 认证配置约定 ...
 *
 * 主要是解决 各种授权服务器组合的问题 ...
 */
public interface AuthConfigConstant {

    public static final String AUTH_SERVER_WITH_VERSION_PREFIX = "/auth/v1";

    public static final String RESOURCE_PATTERN_PREFIX = "/api";

    public static final String TOKEN_PATTERN_PREFIX = "/token";

    /**
     * 表单不分离配置
     */
    final class ENABLE_FORM_LOGIN_NO_SEPARATION implements AuthConfigConstant {

        private ENABLE_FORM_LOGIN_NO_SEPARATION() {

        }
        public static final ENABLE_FORM_LOGIN_NO_SEPARATION INSTANCE = new ENABLE_FORM_LOGIN_NO_SEPARATION();
    }

    /**
     * 表单分离配置
     */
    final class ENABLE_FORM_LOGIN_SEPARATION implements AuthConfigConstant {

        private ENABLE_FORM_LOGIN_SEPARATION() {

        }
        public static final ENABLE_FORM_LOGIN_SEPARATION INSTANCE = new ENABLE_FORM_LOGIN_SEPARATION();
    }



}
