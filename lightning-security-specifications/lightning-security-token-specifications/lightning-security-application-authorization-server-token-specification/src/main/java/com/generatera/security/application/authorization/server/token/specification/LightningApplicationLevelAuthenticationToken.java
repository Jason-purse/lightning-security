package com.generatera.security.application.authorization.server.token.specification;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.generatera.security.server.token.specification.LightningToken.LightningAccessToken;
import com.generatera.security.server.token.specification.LightningToken.LightningRefreshToken;
import lombok.AllArgsConstructor;
import org.springframework.lang.Nullable;

/**
 * @author FLJ
 * @date 2023/1/3
 * @time 11:08
 * @Description Lightning application level Authentication Token
 */
public interface LightningApplicationLevelAuthenticationToken  {

    /**
     * 用于存储在请求中,由 LightningSecurityContextRepository 对上下文进行存储 ..
     */
    public static final String TOKEN_REQUEST_ATTRIBUTE = "lightning.application.level.authentication.token";

    @Nullable
    LightningAccessToken accessToken();


    @Nullable
    LightningRefreshToken refreshToken();


    static LightningApplicationLevelAuthenticationToken of(LightningAccessToken accessToken, LightningRefreshToken refreshToken) {
        return new DefaultLightningApplicationLevelAuthenticationToken(accessToken,refreshToken);
    }



}

@AllArgsConstructor
class DefaultLightningApplicationLevelAuthenticationToken implements LightningApplicationLevelAuthenticationToken {

    private LightningAccessToken accessToken;

    private LightningRefreshToken refreshToken;


    @Override
    @JsonGetter
    public LightningAccessToken accessToken() {
        return accessToken;
    }

    @Override
    @JsonGetter
    public LightningRefreshToken refreshToken() {
        return refreshToken;
    }

}
