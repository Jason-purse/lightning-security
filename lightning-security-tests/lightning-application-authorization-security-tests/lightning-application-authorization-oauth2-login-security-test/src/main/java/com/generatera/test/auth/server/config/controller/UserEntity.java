package com.generatera.test.auth.server.config.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;


/**
 * @author Sun.
 * @Description 用户中心 - 用户表
 */
@Entity
@Table(name = "uc_users")
@AllArgsConstructor
@NoArgsConstructor
@Data
@GenericGenerator(name = "jpa-uuid", strategy = "uuid")
public class UserEntity {


    @Id
    @GeneratedValue(generator = "jpa-uuid")
    private String id;


    /**
     * 授权用户唯一标识
     */
    @Column(unique = true)
    private String openId;

    /**
     * 名字
     */
    private String name;


    /**
     * 所属组织id
     */
    private String orgId;

    /**
     * 是否删除
     */
    private Boolean deleted;


}
