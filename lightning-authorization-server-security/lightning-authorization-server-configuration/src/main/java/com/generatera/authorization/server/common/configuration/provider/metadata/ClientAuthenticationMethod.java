package com.generatera.authorization.server.common.configuration.provider.metadata;

import org.springframework.util.Assert;

import java.io.Serializable;

public final class ClientAuthenticationMethod implements Serializable {
    private static final long serialVersionUID = 570L;
    /** @deprecated */
    @Deprecated
    public static final ClientAuthenticationMethod BASIC = new ClientAuthenticationMethod("basic");
    public static final ClientAuthenticationMethod POST = new ClientAuthenticationMethod("post");
    private final String value;

    public ClientAuthenticationMethod(String value) {
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
            ClientAuthenticationMethod that = (ClientAuthenticationMethod)obj;
            return this.getValue().equalsIgnoreCase(that.getValue());
        } else {
            return false;
        }
    }

    public int hashCode() {
        return this.getValue().hashCode();
    }
}