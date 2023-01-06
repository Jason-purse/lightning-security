package com.generatera.security.server.token.specification.format.plain;


import com.generatera.security.server.token.specification.LightningToken;

import java.time.Instant;

public class DefaultPlainToken extends LightningToken.PlainToken {

    protected DefaultPlainToken(String tokenValue) {
        super(tokenValue);
    }

    protected DefaultPlainToken(String tokenValue, Instant issuedAt, Instant expiresAt) {
        super(tokenValue, issuedAt, expiresAt);
    }
}
