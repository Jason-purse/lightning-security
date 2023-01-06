package com.generatera.security.server.token.specification.generators;

import com.generatera.security.server.token.specification.LightningServerTokenGenerator;
import com.generatera.security.server.token.specification.LightningTokenType.LightningTokenValueType;
import com.generatera.security.server.token.specification.type.LightningAccessToken;
import org.jetbrains.annotations.NotNull;

/**
 * @author FLJ
 * @date 2023/1/6
 * @time 13:50
 * @Description Lightning access token generator
 */
public interface LightningAccessTokenGenerator extends LightningServerTokenGenerator<LightningAccessToken> {
    @NotNull
    abstract LightningTokenValueType getTokenValueType();
}
