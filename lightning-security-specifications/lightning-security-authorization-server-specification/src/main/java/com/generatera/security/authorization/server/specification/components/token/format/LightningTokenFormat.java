package com.generatera.security.authorization.server.specification.components.token.format;
/**
 * @author FLJ
 * @date 2023/1/6
 * @time 12:57
 * @Description Token 格式,例如 JWT 构建的,或者简单的(UUID 构建的)
 */
public class LightningTokenFormat {

    public final static LightningTokenFormat JWT = new LightningTokenFormat();

    public final static LightningTokenFormat PLAIN = new LightningTokenFormat();

}
