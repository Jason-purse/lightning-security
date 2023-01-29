package com.generatera.authorization.application.server.config;

import com.jianyue.lightning.exception.ExceptionStatus;
import lombok.Data;

/**
 * 应用级别的认证错误约束
 */

@Data
public class ApplicationAuthErrorConstant implements ExceptionStatus {

    private final static String DEFAULT_LABEL_NAME = "auth_application_server_auth_error";

    private final String label = DEFAULT_LABEL_NAME;

    private final String identify;

    private final Integer value;

    private ApplicationAuthErrorConstant(String identify, Integer value) {
        this.identify = identify;
        this.value = value;
    }

    @Override
    public String label() {
        return label;
    }

    @Override
    public String identify() {
        return identify;
    }

    @Override
    public Integer value() {
        return value;
    }

    /**
     * 错误的凭证(例如 用户名错误 / 密码错误 )
     */
    public static ApplicationAuthErrorConstant AUTH_BAD_CREDENTIALS_ERROR = new ApplicationAuthErrorConstant("AUTH_BAD_CREDENTIALS_ERROR", 401);

    /**
     * 状态异常
     */
    public static ApplicationAuthErrorConstant ACCOUNT_STATUS_EXCEPTION_ERROR = new ApplicationAuthErrorConstant("AUTH_ACCOUNT_STATUS_ERROR", 401);


    /**
     * 账户过期
     */
    public static ApplicationAuthErrorConstant ACCOUNT_EXPIRED_EXCEPTION_ERROR = new ApplicationAuthErrorConstant("AUTH_ACCOUNT_EXPIRED_ERROR", 401);


    /**
     * 账户被锁
     */
    public static ApplicationAuthErrorConstant ACCOUNT_LOCKED_EXCEPTION_ERROR = new ApplicationAuthErrorConstant("AUTH_ACCOUNT_LOCKED_ERROR", 401);


    /**
     * 账户不存在,一般用不上,为了保护账户密码的重要性 .. 很少向外提供账户的详细信息 ..
     */
    public static ApplicationAuthErrorConstant ACCOUNT_NOT_FOUND_EXCEPTION_ERROR = new ApplicationAuthErrorConstant("AUTH_ACCOUNT_NOT_FOUND_ERROR", 401);
    public static ApplicationAuthErrorConstant AUTH_OTHER_EXCEPTION_ERROR = new ApplicationAuthErrorConstant("AUTH_OTHER_EXCEPTION_ERROR", 401);


    /**
     * oauth2 authentication exception
     */
    public static ApplicationAuthErrorConstant AUTH_OAUTH2_AUTHENTICATION_EXCEPTION = new ApplicationAuthErrorConstant(
            "AUTH_OAUTH2_AUTHENTICATION_EXCEPTION_ERROR", 401
    );

    /**
     * 认证 服务异常
     */
    public static ApplicationAuthErrorConstant AUTH_SERVICE_EXCEPTION = new ApplicationAuthErrorConstant(
            "AUTH_SERVICE_EXCEPTION",
            500
    );

    /**
     * 认证通用失败 ..
     */
    public static ApplicationAuthErrorConstant AUTH_COMMON_FAILURE_EXCEPTION = new ApplicationAuthErrorConstant(
            "AUTH_FAILURE",
            401
    );
}
