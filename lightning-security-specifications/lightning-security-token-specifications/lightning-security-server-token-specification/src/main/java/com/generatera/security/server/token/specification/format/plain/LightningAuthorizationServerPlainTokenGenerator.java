package com.generatera.security.server.token.specification.format.plain;


import com.generatera.security.server.token.specification.LightningAuthorizationServerSecurityContext;
import com.generatera.security.server.token.specification.LightningToken;
import com.generatera.security.server.token.specification.LightningTokenGenerator;

public interface LightningAuthorizationServerPlainTokenGenerator extends LightningTokenGenerator<LightningToken.PlainToken, LightningAuthorizationServerSecurityContext> {

}
