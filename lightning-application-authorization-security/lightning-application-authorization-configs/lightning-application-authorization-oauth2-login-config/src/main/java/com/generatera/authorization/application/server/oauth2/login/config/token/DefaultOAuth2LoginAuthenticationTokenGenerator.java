//package com.generatera.authorization.application.server.oauth2.login.config.token;
//
//import com.nimbusds.jose.jwk.source.JWKSource;
//import com.nimbusds.jose.proc.SecurityContext;
//import org.springframework.util.Assert;
//
//public class DefaultOAuth2LoginAuthenticationTokenGenerator implements LightningOAuth2LoginAuthenticationTokenGenerator {
//
//    private final DefaultAuthenticationTokenGenerator delegate;
//    public DefaultOAuth2LoginAuthenticationTokenGenerator(JWKSource<SecurityContext> source) {
//        Assert.notNull(source,"source must not be null !!!");
//        this.delegate = new DefaultAuthenticationTokenGenerator(source);
//    }
//
//
//    @Override
//    public LightningAuthenticationToken generate(LightningAuthenticationSecurityContext securityContext) {
//        return delegate.generate(securityContext);
//    }
//}
