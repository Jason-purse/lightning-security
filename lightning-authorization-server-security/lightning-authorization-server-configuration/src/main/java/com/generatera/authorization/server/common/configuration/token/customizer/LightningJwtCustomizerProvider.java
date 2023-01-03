package com.generatera.authorization.server.common.configuration.token.customizer;

import com.generatera.authorization.server.common.configuration.token.customizer.jwt.LightningJwtCustomizer;

import java.util.function.Supplier;

/**
 * Lightning Jwt Customizer provider
 */
public interface LightningJwtCustomizerProvider extends Supplier<LightningJwtCustomizer> {

}
