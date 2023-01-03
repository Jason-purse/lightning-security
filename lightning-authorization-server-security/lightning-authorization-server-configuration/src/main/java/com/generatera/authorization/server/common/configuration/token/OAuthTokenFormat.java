package com.generatera.authorization.server.common.configuration.token;

import org.springframework.util.Assert;

import java.io.Serializable;
/**
 * @author FLJ
 * @date 2023/1/3
 * @time 14:08
 * @Description Token Format
 */
public final class OAuthTokenFormat implements Serializable {
    public static final OAuthTokenFormat SELF_CONTAINED;
    public static final OAuthTokenFormat REFERENCE;
    private final String value;

    public OAuthTokenFormat(String value) {
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
            OAuthTokenFormat that = (OAuthTokenFormat)obj;
            return this.getValue().equals(that.getValue());
        } else {
            return false;
        }
    }

    public int hashCode() {
        return this.getValue().hashCode();
    }

    static {
        SELF_CONTAINED = new OAuthTokenFormat("self-contained");
        REFERENCE = new OAuthTokenFormat("reference");
    }
}