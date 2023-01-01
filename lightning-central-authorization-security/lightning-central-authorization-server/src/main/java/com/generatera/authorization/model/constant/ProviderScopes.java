package com.generatera.authorization.model.constant;
/**
 * @author FLJ
 * @date 2022/12/27
 * @time 10:26
 * @Description 提供商scopes
 */
public interface ProviderScopes {

    /**
     * 登录的scopes ..
     */
    enum LOGIN_IN_SCOPES {
        /**
         * 邮箱 邮箱号码
         */
        EMAIL,
        /**
         * 电话 电话号码
         */
        TELEPHONE,
        /**
         * 身份证 身份证信息
         */
        ID_CARD,
        /**
         * openId  和个人信息关联的id
         */
        OPENID,
        /**
         * profile  查看您的个人信息，包括您已公开的任何个人信息
         */
        PROFILE
    }
}
