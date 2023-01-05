package com.generatera.authorization.server.common.configuration.token;

import com.fasterxml.jackson.annotation.JsonGetter;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.Nullable;

/**
 * @author FLJ
 * @date 2023/1/3
 * @time 11:08
 * @Description Lightning Authentication Token
 */
public interface LightningAuthenticationToken  {

    /**
     * 用于存储在请求中,由 LightningSecurityContextRepository 对上下文进行存储 ..
     */
    public static final String TOKEN_REQUEST_ATTRIBUTE = "lightning.authentication.token";

    @Nullable
    LightningToken accessToken();


    @Nullable
    LightningToken refreshToken();


    @Nullable
    LightningToken otherToken();


    static LightningAuthenticationToken of(LightningToken accessToken, LightningToken refreshToken) {
        return new DefaultLightningAuthenticationToken(accessToken,refreshToken,null);
    }

    static LightningAuthenticationToken of(LightningToken accessToken,LightningToken refreshToken,LightningToken otherToken) {
        return new DefaultLightningAuthenticationToken(accessToken,refreshToken,otherToken);
    }

    static LightningAuthenticationToken of(LightningToken token) {
        return new DefaultLightningAuthenticationToken(null,null,token);
    }


}

@AllArgsConstructor
class DefaultLightningAuthenticationToken implements LightningAuthenticationToken {

    private LightningToken accessToken;

    private LightningToken refreshToken;


    private LightningToken otherToken;

    @Override
    @JsonGetter
    public LightningToken accessToken() {
        return accessToken;
    }

    @Override
    @JsonGetter
    public LightningToken refreshToken() {
        return refreshToken;
    }

    @JsonGetter
    public LightningToken otherToken() {
        return otherToken;
    }
}
