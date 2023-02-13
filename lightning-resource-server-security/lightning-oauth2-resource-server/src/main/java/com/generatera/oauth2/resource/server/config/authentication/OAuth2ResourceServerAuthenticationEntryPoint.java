package com.generatera.oauth2.resource.server.config.authentication;

import com.generatera.resource.server.config.authentication.LightningResourceServerAuthenticationEntryPoint;

/**
 * @author FLJ
 * @date 2023/2/13
 * @time 10:07
 * @Description 认证失败 响应处理 ..
 */
public class OAuth2ResourceServerAuthenticationEntryPoint implements LightningResourceServerAuthenticationEntryPoint {
    /**
     * 访问拒绝 ..
     * "无效token"
     */
    protected String invalidTokenErrorMessage;

    protected String accessDeniedErrorMessage;

    public void setInvalidTokenErrorMessage(String invalidTokenErrorMessage) {
        this.invalidTokenErrorMessage = invalidTokenErrorMessage;
    }

    public void setAccessDeniedErrorMessage(String accessDeniedErrorMessage) {
        this.accessDeniedErrorMessage = accessDeniedErrorMessage;
    }

    @Override
    public String getAccessDeniedErrorMessage() {
        return accessDeniedErrorMessage;
    }

    @Override
    public String getInvalidTokenErrorMessage() {
        return invalidTokenErrorMessage;
    }
}
