package com.generatera.authorization.application.server.config.authentication;

import org.jetbrains.annotations.Nullable;

/**
 * @author FLJ
 * @date 2023/1/29
 * @time 11:59
 * @Description 默认 抽象实现 ..
 */
public class DefaultLightningAbstractAuthenticationEntryPoint implements LightningAbstractAuthenticationEntryPoint {
    /**
     * 是否启用 认证详情错误描述,一般默认没有开启 ..
     */
    private final boolean enableAuthDetails;

    /**
     * 单独启用它没有任何意义
     */
    private final boolean enableAccountStatusDetails;

    private String accountStatusMessage;

    private String loginSuccessMessage;

    private String loginFailureMessage;

    private String badCredentialsMessage;

    private String accountLockedMessage;

    private String accountExpiredMessage;

    private String unAuthenticatedMessage;

    public DefaultLightningAbstractAuthenticationEntryPoint(boolean enableAuthDetails,boolean enableAccountStatusDetails) {
        this.enableAuthDetails = enableAuthDetails;
        this.enableAccountStatusDetails = enableAccountStatusDetails;
    }
    public DefaultLightningAbstractAuthenticationEntryPoint(boolean enableAuthDetails) {
       this(enableAuthDetails,false);
    }

    public DefaultLightningAbstractAuthenticationEntryPoint() {
        this(false,false);
    }

    public void setAccountStatusMessage(String accountStatusMessage) {
        this.accountStatusMessage = accountStatusMessage;
    }

    public void setBadCredentialsMessage(String badCredentialsMessage) {
        this.badCredentialsMessage = badCredentialsMessage;
    }

    public void setLoginSuccessMessage(String loginSuccessMessage) {
        this.loginSuccessMessage = loginSuccessMessage;
    }

    public void setLoginFailureMessage(String loginFailureMessage) {
        this.loginFailureMessage = loginFailureMessage;
    }

    public void setAccountExpiredMessage(String accountExpiredMessage) {
        this.accountExpiredMessage = accountExpiredMessage;
    }

    public void setUnAuthenticatedMessage(String unAuthenticatedMessage) {
        this.unAuthenticatedMessage = unAuthenticatedMessage;
    }

    public void setAccountLockedMessage(String accountLockedMessage) {
        this.accountLockedMessage = accountLockedMessage;
    }

    @Override
    public boolean enableAuthDetails() {
        return enableAuthDetails;
    }

    @Nullable
    @Override
    public String getLoginSuccessMessage() {
        return loginSuccessMessage;
    }

    @Nullable
    @Override
    public String getBadCredentialsMessage() {
        return badCredentialsMessage;
    }

    @Nullable
    @Override
    public String getAccountExceptionMessage() {
        return accountStatusMessage;
    }

    @Override
    public boolean enableAccountStatusDetails() {
        return enableAccountStatusDetails;
    }

    @Nullable
    @Override
    public String getAccountExpiredMessage() {
        return accountExpiredMessage;
    }

    @Nullable
    @Override
    public String getAccountLockedMessage() {
        return accountLockedMessage;
    }

    @Nullable
    @Override
    public String getLoginFailureMessage() {
        return loginFailureMessage;
    }

    @Override
    public String getUnAuthenticatedMessage() {
        return unAuthenticatedMessage;
    }
}

