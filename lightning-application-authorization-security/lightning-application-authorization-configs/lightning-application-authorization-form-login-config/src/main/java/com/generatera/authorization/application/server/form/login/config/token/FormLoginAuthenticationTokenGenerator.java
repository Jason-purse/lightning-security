package com.generatera.authorization.application.server.form.login.config.token;

import com.generatera.authorization.server.common.configuration.token.LightningAuthenticationTokenGenerator;
/**
 * @author FLJ
 * @date 2023/1/3
 * @time 14:21
 * @Description 请注意,当与oauth2-login-config 联合使用的时候
 * 将自动代理到 OAuth2 Token 生成器上处理 ...
 *
 * 但是如果你自定义了此生成器,需要注意生成token的规则 ..
 *
 * 例如auth2-login 生成的 token 和 表单登录的token 互通 ...
 *
 */
public interface FormLoginAuthenticationTokenGenerator extends LightningAuthenticationTokenGenerator {
}
