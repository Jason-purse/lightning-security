package com.generatera.authorization.application.server.form.login.config.token;

import com.generatera.authorization.server.common.configuration.provider.ProviderContext;
import com.generatera.security.authorization.server.specification.TokenSettings;
import com.generatera.security.server.token.specification.LightningSecurityContext;
import com.generatera.security.server.token.specification.LightningTokenType.LightningAuthenticationTokenType;
import org.springframework.security.core.Authentication;

public interface FormLoginSecurityContext  extends LightningSecurityContext {




    static FormLoginSecurityContext of(LightningAuthenticationTokenType tokenType,
                                       Authentication authentication,
                                       ProviderContext providerContext,
                                       TokenSettings tokenSettings) {

        //return new DefaultFormLoginSecurityContext(tokenType,
        //        authentication,
        //        providerContext,
        //        tokenSettings
        //);
        return null;
    }
}

//@AllArgsConstructor
//class DefaultFormLoginSecurityContext implements FormLoginSecurityContext {
//
//
//    private LightningAuthenticationTokenType tokenType;
//
//    private Authentication authentication;
//
//    private ProviderContext providerContext;
//
//    private TokenSettings tokenSettings;
//
//    @Override
//    public LightningAuthenticationTokenType getTokenType() {
//        return tokenType;
//    }
//
//    @Override
//    public Authentication getAuthentication() {
//        return authentication;
//    }
//
//    @Override
//    public ProviderContext getProviderContext() {
//        return providerContext;
//    }
//
//    @Override
//    public TokenSettings getTokenSettings() {
//        return tokenSettings;
//    }
//}
