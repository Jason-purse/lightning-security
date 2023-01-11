package com.generatera.security.authorization.server.specification.components.token.format.jwt.customizer;

/**
 * @author FLJ
 * @date 2023/1/11
 * @time 16:06
 * @Description Lightning Jwt 定制化器 ...
 */
public interface LightningJwtCustomizer {

	/**
	 * 可以使用此定制器进行 jwt claims / headers 的深度定制 ...
	 *
	 * 例如在 claims中添加 authorities 属性 ...等等操作
	 *
	 * @see JwtEncodingContext
	 * @param context jwt 编辑上下文 ...
	 */
	void customizeToken(JwtEncodingContext context);
	
}
