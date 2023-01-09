package com.generatera.security.authorization.server.specification.components.authorization;

import org.springframework.util.Assert;

import java.io.Serializable;

public class LightningAuthError implements Serializable {
    private static final long serialVersionUID = 570L;
    private final String errorCode;
    private final String description;
    private final String uri;

    public LightningAuthError(String errorCode) {
        this(errorCode, (String)null, (String)null);
    }

    public LightningAuthError(String errorCode, String description, String uri) {
        Assert.hasText(errorCode, "errorCode cannot be empty");
        this.errorCode = errorCode;
        this.description = description;
        this.uri = uri;
    }

    public final String getErrorCode() {
        return this.errorCode;
    }

    public final String getDescription() {
        return this.description;
    }

    public final String getUri() {
        return this.uri;
    }

    public String toString() {
        return "[" + this.getErrorCode() + "] " + (this.getDescription() != null ? this.getDescription() : "");
    }
}