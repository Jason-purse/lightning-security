package com.generatera.model.entity;


import com.generatera.authorization.server.common.configuration.annotations.SafeSerialize;
import com.generatera.central.oauth2.authorization.server.configuration.model.entity.UserAuthority;
import com.generatera.security.authorization.server.specification.LightningUserPrincipal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SafeSerialize
public class LightningOAuth2UserDetails implements LightningUserPrincipal, Cloneable {

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
        this.password = null;
    }

    @Override
    public LightningOAuth2UserDetails clone() {
        try {
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            return (LightningOAuth2UserDetails) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
