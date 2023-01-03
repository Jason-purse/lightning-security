package com.generatera.authorization.model.entity;

import com.generatera.authorization.server.oauth2.configuration.model.ext.jackson.SafeSerialize;
import com.google.common.collect.Lists;
import com.jianyue.lightning.boot.starter.generic.crud.service.entity.JpaBaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.io.Serial;
import java.util.Collection;


/**
 * @author Sun.
 * @Description oauth2 授权用户实体
 */
@Entity
@Table(name = "uaa_users")
@AllArgsConstructor
@NoArgsConstructor
@Data
@SafeSerialize
public class OAuth2UserEntity extends JpaBaseEntity<Long>  implements UserDetails, CredentialsContainer, Cloneable{


    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    /**
     * 授权用户唯一标识
     */
    @Column(unique = true)
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
    @Column(unique = true)
    private Long mobile;

    /**
     * 身份证
     */
    @Column( unique = true)
    private String idCard;

    /**
     * 邮箱
     */
    @Column(unique = true)
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



    @Override
    public Long getId() {
        return this.id;
    }

    @Override
    public void eraseCredentials() {

    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Lists.newArrayList();
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return this.accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }


}
