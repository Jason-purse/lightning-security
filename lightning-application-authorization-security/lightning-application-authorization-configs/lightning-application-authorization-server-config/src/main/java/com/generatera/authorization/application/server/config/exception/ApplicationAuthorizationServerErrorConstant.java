package com.generatera.authorization.application.server.config.exception;

import com.jianyue.lightning.exception.ExceptionStatus;


class ApplicationAuthorizationServerErrorConstant implements ExceptionStatus {
    private static final String DEFAULT_LABEL = "application-authorization-server-error";
    private final String label = DEFAULT_LABEL;
    private final String identity;
    private final Integer value;

    public ApplicationAuthorizationServerErrorConstant(String identity, Integer code) {
        this.identity = identity;
        this.value = code;
    }

    @Override
    public String label() {
        return label;
    }

    @Override
    public String identify() {
        return identity;
    }

    @Override
    public Integer value() {
        return value;
    }


    public static ApplicationAuthorizationServerErrorConstant NO_TOKEN_AND_SAVE_SECURITY_CONTEXT_EXCEPTION =
            new ApplicationAuthorizationServerErrorConstant("NO_TOKEN_AND_SAVE_SECURITY_CONTEXT_EXCEPTION", 500);

    public static ApplicationAuthorizationServerErrorConstant NO_AUTHENTICATION_PARSER_EXCEPTION =
            new ApplicationAuthorizationServerErrorConstant("NO_AUTHENTICATION_PARSER_EXCEPTION",500);
}
