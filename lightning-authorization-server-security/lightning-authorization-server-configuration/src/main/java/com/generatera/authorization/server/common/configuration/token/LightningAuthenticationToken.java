package com.generatera.authorization.server.common.configuration.token;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author FLJ
 * @date 2023/1/3
 * @time 11:08
 * @Description Lightning Authentication Token
 */
public interface LightningAuthenticationToken  {

    @NotNull
    LightningToken accessToken();


    @Nullable
    LightningToken refreshToken();


    static LightningAuthenticationToken of(LightningToken accessToken, LightningToken refreshToken) {
        return new DefaultLightningAuthenticationToken(accessToken,refreshToken);
    }
}

@AllArgsConstructor
class DefaultLightningAuthenticationToken implements LightningAuthenticationToken {

    private LightningToken accessToken;

    private LightningToken refreshToken;

    @Override
    public LightningToken accessToken() {
        return accessToken;
    }

    @Override
    public LightningToken refreshToken() {
        return refreshToken;
    }
}
