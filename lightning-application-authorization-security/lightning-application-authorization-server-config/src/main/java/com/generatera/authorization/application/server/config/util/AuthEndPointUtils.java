package com.generatera.authorization.application.server.config.util;

import com.generatera.security.authorization.server.specification.components.authorization.LightningAuthError;
import com.generatera.security.authorization.server.specification.components.authorization.LightningAuthenticationException;

public class AuthEndPointUtils {
    public static void throwError(String errorCode, String parameterName, String errorUri) {
        LightningAuthError error = new LightningAuthError(errorCode, "Auth  Parameter: " + parameterName, errorUri);
        throw new LightningAuthenticationException(error);
    }
}
