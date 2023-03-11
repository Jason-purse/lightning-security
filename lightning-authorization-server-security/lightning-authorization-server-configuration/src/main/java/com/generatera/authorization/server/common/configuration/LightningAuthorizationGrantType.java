package com.generatera.authorization.server.common.configuration;

import org.springframework.util.Assert;

import java.io.Serializable;

public final class LightningAuthorizationGrantType implements Serializable {

    public static final LightningAuthorizationGrantType REFRESH_TOKEN = new LightningAuthorizationGrantType("refresh_token");

    /**
     * 自定义
     */
    public static final LightningAuthorizationGrantType ACCESS_TOKEN = new LightningAuthorizationGrantType("access_token");

    private final String value;

    public LightningAuthorizationGrantType(String value) {
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
            LightningAuthorizationGrantType that = (LightningAuthorizationGrantType)obj;
            return this.getValue().equals(that.getValue());
        } else {
            return false;
        }
    }

    public int hashCode() {
        return this.getValue().hashCode();
    }
}
