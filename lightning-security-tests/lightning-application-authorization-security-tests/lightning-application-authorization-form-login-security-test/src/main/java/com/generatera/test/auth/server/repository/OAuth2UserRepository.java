package com.generatera.test.auth.server.repository;

import com.generatera.test.auth.server.entity.OAuth2UserEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;


/**
 * @author Sun.
 */
public interface OAuth2UserRepository extends CrudRepository<OAuth2UserEntity,Long> {

    /**
     * 根据用户名查找
     * @param username
     * @return
     */
    Optional<OAuth2UserEntity> findByUsernameAndDeletedIsFalse(String username);
}
