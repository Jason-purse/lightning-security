package com.generatera.security.authorization.server.specification.components.token;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * 主要是代理所有 LightningTokenGenerator 的 token 生成工作 ...
 */
public final class DelegatingLightningTokenGenerator implements LightningTokenGenerator<LightningToken> {
    private final List<LightningTokenGenerator<LightningToken>> tokenGenerators;

    @SafeVarargs
    public DelegatingLightningTokenGenerator(LightningTokenGenerator<? extends LightningToken>... tokenGenerators) {
        Assert.notEmpty(tokenGenerators, "tokenGenerators cannot be empty");
        Assert.noNullElements(tokenGenerators, "tokenGenerator cannot be null");
        this.tokenGenerators = Collections.unmodifiableList(asList(tokenGenerators));
    }

    @Nullable
    public LightningToken generate(LightningSecurityTokenContext context) {
        Iterator<LightningTokenGenerator<LightningToken>> var2 = this.tokenGenerators.iterator();

        LightningToken token;
        do {
            if (!var2.hasNext()) {
                return null;
            }

            LightningTokenGenerator<LightningToken> tokenGenerator = var2.next();
            token = tokenGenerator.generate(context);
        } while(token == null);

        return token;
    }

    @SuppressWarnings("unchecked")
    private static List<LightningTokenGenerator<LightningToken>> asList(LightningTokenGenerator<? extends LightningToken>... tokenGenerators) {
        List<LightningTokenGenerator<LightningToken>> tokenGeneratorList = new ArrayList<>();
        int var3 = tokenGenerators.length;

        for (LightningTokenGenerator<? extends LightningToken> tokenGenerator : tokenGenerators) {
            tokenGeneratorList.add(((LightningTokenGenerator<LightningToken>) tokenGenerator));
        }

        return tokenGeneratorList;
    }
}