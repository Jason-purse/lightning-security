package com.generatera.security.server.token.specification.generators;

import com.generatera.security.server.token.specification.LightningServerTokenGenerator;
import com.generatera.security.server.token.specification.LightningToken;
import com.generatera.security.server.token.specification.LightningTokenType;
import com.generatera.security.server.token.specification.LightningTokenType.LightningTokenValueType;
import org.jetbrains.annotations.NotNull;

/**
 * @author FLJ
 * @date 2023/1/6
 * @time 14:55
 * @Description authorization server refresh token generator
 */
public interface LightningRefreshTokenGenerator extends LightningServerTokenGenerator<LightningToken.LightningRefreshToken> {
    @NotNull
    abstract LightningTokenValueType getTokenValueType();
}
