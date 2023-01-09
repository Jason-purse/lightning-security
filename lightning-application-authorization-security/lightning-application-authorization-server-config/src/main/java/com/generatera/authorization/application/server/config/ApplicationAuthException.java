package com.generatera.authorization.application.server.config;

import com.jianyue.lightning.exception.AbstractApplicationException;

public class ApplicationAuthException extends AbstractApplicationException {

    public ApplicationAuthException(Integer code, String message, Throwable throwable) {
        super(code, message, throwable);
    }

    public ApplicationAuthException(Integer code, String message) {
        super(code, message, null);
    }

    /**
     * 坏的 凭证异常 ..
     *
     * @return 返回凭证异常
     */
    public static ApplicationAuthException badCredentialsException() {
        return of(ApplicationAuthErrorConstant.AUTH_BAD_CREDENTIALS_ERROR);
    }

    /**
     * 状态异常
     * @return 返回状态异常
     */

    public static ApplicationAuthException accountStatusException() {
        return of(ApplicationAuthErrorConstant.ACCOUNT_STATUS_EXCEPTION_ERROR);
    }

    public static ApplicationAuthException authOtherException() {
        return of(ApplicationAuthErrorConstant.AUTH_OTHER_EXCEPTION_ERROR);
    }

    public static ApplicationAuthException accountExpiredException() {
        return of(ApplicationAuthErrorConstant.ACCOUNT_EXPIRED_EXCEPTION_ERROR);
    }

    public static ApplicationAuthException accountLockedException() {
        return of(ApplicationAuthErrorConstant.ACCOUNT_LOCKED_EXCEPTION_ERROR);
    }

    public static ApplicationAuthException accountNotFoundException() {
        return of(ApplicationAuthErrorConstant.ACCOUNT_NOT_FOUND_EXCEPTION_ERROR);
    }

    public static ApplicationAuthException auth2AuthenticationException() {
        return of(ApplicationAuthErrorConstant.AUTH_OAUTH2_AUTHENTICATION_EXCEPTION);
    }

    public static ApplicationAuthException of(ApplicationAuthErrorConstant errorConstant) {
        return new ApplicationAuthException(errorConstant.getValue(), errorConstant.getIdentify());
    }
}
