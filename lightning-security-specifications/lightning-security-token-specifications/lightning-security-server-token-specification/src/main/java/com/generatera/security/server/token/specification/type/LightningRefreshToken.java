package com.generatera.security.server.token.specification.type;

import com.generatera.security.server.token.specification.DelegateLightningToken;
import com.generatera.security.server.token.specification.LightningToken;

/**
 * @author FLJ
 * @date 2023/1/6
 * @time 13:09
 * @Description 普通刷新Token
 */
public class LightningRefreshToken extends DelegateLightningToken implements LightningToken.LightningRefreshToken {

    public LightningRefreshToken(LightningToken delegate) {
        super(delegate);
    }
}
