package com.generatera.authorization.model.entity;

import com.generatera.authorization.server.common.configuration.model.ext.UserAuthority;
import com.generatera.authorization.server.common.configuration.model.ext.jackson.SafeSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SafeSerialize
public class LightningOAuth2UserDetails implements UserDetails,CredentialsContainer, Cloneable {

    private Long id;


    /**
     * 授权用户唯一标识
     */
    private String openId;
    /**
     * 用户名
     */
    private String username;

    /**
     * 加密密码
     */
    private String password;

    /**
     * 名字
     */
    private String name;

    /**
     * 手机号
     */
    private Long mobile;

    /**
     * 身份证
     */
    private String idCard;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 生日
     */
    private String birthday;

    /**
     * 性别
     */
    private String sex;

    /**
     * 创建人id
     */
    private String createdId;


    /**
     * 是否删除
     */
    private Boolean deleted;

    private boolean enabled;
    private boolean accountNonExpired ;
    private boolean credentialsNonExpired;
    private boolean accountNonLocked;


    private Set<UserAuthority> authorities = new HashSet<>();


    @Override
    public void eraseCredentials() {

    }
}
