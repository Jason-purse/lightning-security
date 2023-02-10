package com.generatera.security.authorization.server.specification.components.token.format.jwt.customizer;

import com.generatera.security.authorization.server.specification.components.token.LightningTokenCustomizer;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.JwtEncodingContext;

/**
 * @author FLJ
 * @date 2023/1/11
 * @time 16:06
 * @Description Lightning Jwt 定制化器 ...
 *
 * 根据 JwtEncodingContext 进行 jwt claims 定制 ..
 */
public interface LightningJwtCustomizer extends LightningTokenCustomizer<JwtEncodingContext> {

	/**
	 * 可以使用此定制器进行 jwt claims / headers 的深度定制 ...
	 *
	 * 例如在 claims中添加 authorities 属性 ...等等操作
	 *
	 * @see JwtEncodingContext
	 * @param context jwt 编辑上下文 ...
	 */
	void customizeToken(JwtEncodingContext context);

	@Override
	default void customize(JwtEncodingContext tokenContext) {
		customizeToken(tokenContext);
	}
}
