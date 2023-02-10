package com.generatera.security.authorization.server.specification.components.token.format.plain;


import com.generatera.security.authorization.server.specification.components.token.LightningToken;

import java.time.Instant;

public class DefaultPlainToken extends LightningToken.PlainToken {

    public DefaultPlainToken(String tokenValue) {
        super(tokenValue);
    }

    public  DefaultPlainToken(String tokenValue, Instant issuedAt, Instant expiresAt) {
        super(tokenValue, issuedAt, expiresAt);
    }
}
