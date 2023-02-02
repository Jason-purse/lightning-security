package com.generatera.authorization.application.server.config;

import org.springframework.util.Assert;

/**
 * @author FLJ
 * @date 2023/1/29
 * @time 14:10
 * @Description
 */
public interface LoginGrantType {

    static LoginGrantType of(String loginGrantType) {
        return new DefaultLoginGrantType(loginGrantType);
    }

    public String value();

    public static final LoginGrantType FORM_LOGIN = new DefaultLoginGrantType("form_login");

    public static final LoginGrantType OAUTH2_CLIENT_LOGIN = new DefaultLoginGrantType("oauth2_client_login");
}

class DefaultLoginGrantType implements LoginGrantType {
    private final String value;

    public DefaultLoginGrantType(String value) {
        Assert.hasText(value, "value must not be null !!!");
        this.value = value;
    }

    @Override
    public String value() {
        return value;
    }
}
