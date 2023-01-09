package com.generatera.authorization.application.server.config.exception;

import com.jianyue.lightning.exception.AbstractApplicationException;

/**
 * @author FLJ
 * @date 2023/1/5
 * @time 10:17
 * @Description 表示服务器内部异常 ..
 */
public class ApplicationAuthorizationServerException extends AbstractApplicationException {

    public ApplicationAuthorizationServerException(Integer code, String message) {
        super(code, message,null);
    }
    public ApplicationAuthorizationServerException(Integer code,String message,Throwable throwable) {
        super(code,message,throwable);
    }

    public static ApplicationAuthorizationServerException throwNoTokenForSaveSecurityContextError() {
        return new ApplicationAuthorizationServerException(ApplicationAuthorizationServerErrorConstant.NO_TOKEN_AND_SAVE_SECURITY_CONTEXT_EXCEPTION.value(),ApplicationAuthorizationServerErrorConstant.NO_TOKEN_AND_SAVE_SECURITY_CONTEXT_EXCEPTION.identify());
    }

    public static ApplicationAuthorizationServerException throwNoAuthenticationParserError() {
        return new ApplicationAuthorizationServerException(ApplicationAuthorizationServerErrorConstant.NO_AUTHENTICATION_PARSER_EXCEPTION.value(),ApplicationAuthorizationServerErrorConstant.NO_AUTHENTICATION_PARSER_EXCEPTION.identify());
    }

}
