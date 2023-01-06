//package com.generatera.authorization.server.oauth2.configuration.token;
//
//import com.generatera.authorization.server.common.configuration.token.customizer.jwt.LightningJwtCustomizerHandler;
//import com.generatera.authorization.server.oauth2.configuration.token.impl.DefaultJwtCustomizerHandler;
//
//public interface OAuth2JwtCustomizerHandler extends LightningJwtCustomizerHandler {
//    static LightningJwtCustomizerHandler getJwtCustomizerHandler() {
//
//        LightningJwtCustomizerHandler defaultJwtCustomizerHandler = new DefaultJwtCustomizerHandler();
//        return new OAuth2AuthenticationTokenJwtCustomizerHandler(defaultJwtCustomizerHandler);
//
//
//    }
//
//}
