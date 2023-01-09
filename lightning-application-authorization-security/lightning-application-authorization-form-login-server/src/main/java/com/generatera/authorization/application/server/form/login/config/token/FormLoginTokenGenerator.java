package com.generatera.authorization.application.server.form.login.config.token;

import com.generatera.security.authorization.server.specification.components.token.LightningToken;
import com.generatera.security.authorization.server.specification.components.token.LightningTokenGenerator;

/**
 * @author FLJ
 * @date 2023/1/3
 * @time 14:21
 * @Description 表单登陆Token 生成器 
 *
 */
public interface FormLoginTokenGenerator extends LightningTokenGenerator<LightningToken> {
}
