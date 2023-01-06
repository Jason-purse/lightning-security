package com.generatera.security.server.token.specification.type;

import com.generatera.security.server.token.specification.DelegateLightningToken;
import com.generatera.security.server.token.specification.LightningToken;

/**
 * @author FLJ
 * @date 2023/1/6
 * @time 13:07
 * @Description 普通访问Token ..
 */
public class LightningAccessToken extends DelegateLightningToken implements LightningToken.LightningAccessToken {

    public LightningAccessToken(LightningToken delegate) {
        super(delegate);
    }
}
