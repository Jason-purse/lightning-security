package com.generatera.authorization.server.common.configuration.token.customizer.jwt;

import org.springframework.security.oauth2.jwt.JwtEncodingException;

@FunctionalInterface
public interface JwtEncoder {
    LightningJwt encode(JwtEncoderParameters parameters) throws JwtEncodingException;
}