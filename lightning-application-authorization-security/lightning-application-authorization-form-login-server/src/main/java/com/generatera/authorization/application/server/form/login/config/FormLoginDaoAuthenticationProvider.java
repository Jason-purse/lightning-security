//package com.generatera.authorization.application.server.form.login.config;
//
//import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.userdetails.UserDetails;
///**
// * @author FLJ
// * @date 2023/1/11
// * @time 17:26
// * @Description 将它进行封装 ... 给出一个DaoAuthenticationProvider ..
// */
//public class FormLoginDaoAuthenticationProvider extends DaoAuthenticationProvider {
//
//    @Override
//    protected Authentication createSuccessAuthentication(Object principal, Authentication authentication, UserDetails user) {
//        return super.createSuccessAuthentication(new PlainLightningUserPrincipal(((UserDetails) principal)), authentication, user);
//    }
//}
