package com.generatera.authorization.server.common.configuration;

import org.springframework.util.Assert;

import java.io.Serializable;

public final class AuthorizationGrantType implements Serializable {

    public static final AuthorizationGrantType REFRESH_TOKEN = new AuthorizationGrantType("refresh_token");

    /**
     * 自定义
     */
    public static final AuthorizationGrantType ACCESS_TOKEN = new AuthorizationGrantType("access_token");

    public static final AuthorizationGrantType PASSWORD = new AuthorizationGrantType("password");
    public static final AuthorizationGrantType JWT_BEARER = new AuthorizationGrantType("urn:ietf:params:oauth:grant-type:jwt-bearer");
    private final String value;

    public AuthorizationGrantType(String value) {
        Assert.hasText(value, "value cannot be empty");
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj != null && this.getClass() == obj.getClass()) {
            AuthorizationGrantType that = (AuthorizationGrantType)obj;
            return this.getValue().equals(that.getValue());
        } else {
            return false;
        }
    }

    public int hashCode() {
        return this.getValue().hashCode();
    }
}
